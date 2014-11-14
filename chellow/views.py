import hashlib
from flask import request, Response, g, redirect, render_template
from chellow import app
from chellow.models import Contract, Report, User, set_read_write, db
from sqlalchemy.exc import ProgrammingError
import traceback

def GET_str(name):
    return request.args[name]


def GET_int(name):
    val_str = GET_str(name)
    return int(val_str)

def POST_str(name):
    return request.form[name]

def POST_bool(name):
    fm = request.form
    return name in fm and fm[name] == 'true'



@app.before_request
def check_permissions(*args, **kwargs):
    method = request.method
    path = request.path

    g.user = None

    if method == "GET" and path in ('/', '/static/style.css'):
        return None

    user = None
    auth = request.authorization
    if auth is not None:
        pword_digest = hashlib.md5(auth.password).hexdigest()
        user = User.query.filter(
            User.email_address == auth.username,
            User.password_digest == pword_digest).first()

    if user is None:
        config_contract = Contract.get_non_core_by_name('configuration')
        email = config_contract.make_properties()['ips'][request.remote_addr]
        user = User.query.filter(User.email_address == email).first()

    if user is None:
        user_count = User.query.count()
        if user_count is None or user_count == 0 and \
                request.remote_addr == '127.0.0.1':
            return None
    else:
        g.user = user
        role = user.user_role
        role_code = role.code
        path = request.path
        if role_code == "viewer":
            if path.startswith("/reports/") and \
                    path.endswith("/output/") and \
                    request.method in ("GET", "HEAD"):
                return
        elif role_code == "editor":
            return
        elif role_code == "party-viewer":
            if request.method in ("GET", "HEAD"):
                party = user.party
                market_role_code = party.market_role.code
                if market_role_code == 'C':
                    hhdc_contract_id = GET_int(request, "hhdc_contract_id")
                    hhdc_contract = Contract.get_non_core_by_id(
                        hhdc_contract_id)
                    if hhdc_contract.party == party and (
                            request.path_info + "?" + request.query_string) \
                            .startswith(
                                "/reports/37/output/?hhdc_contract_id="
                                + hhdc_contract.id):
                        return
                elif market_role_code == 'X':
                    if path.startswith(
                            "/supplier_contracts/" + party.id):
                        return

    if user is None:
        return Response(
            'Could not verify your access level for that URL.\n'
            'You have to login with proper credentials', 401,
            {'WWW-Authenticate': 'Basic realm="Chellow"'})
    else:
        return Response('Forbidden', 403)


@app.route('/chellow/')
def index():
    # n = 1 / 0
    return 'Hello World'

class ChellowFileItem():
    def __init__(self, f):
        self.f = f

    def getName(self):
        return self.f.filename


class ChellowRequest():
    def __init__(self, request):
        self.request = request

    def getMethod(self):
        return self.request.method

    def getParameter(self, name):
        vals = self.request.values
        return vals[name] if name in vals else None

class Invocation():
    def __init__(self, request, user):
        self.request = request
        self.user = user
        self.req = ChellowRequest(request)

    def getUser(self):
        return self.user

    def hasParameter(self, name):
        return name in self.request.values

    def getString(self, name):
        if name in self.request.values:
            return self.request.values[name]
        else:
            raise Exception("The field '" + name + "' is required.")

    def getBoolean(self, name):
        vals = self.request.values
        return name in vals and vals[name] == 'true'

    def getLong(self, name):
        return int(self.getString(name))

    def getInteger(self, name):
        return int(self.getString(name))

    def getRequest(self):
        return self.req

    def getResponse(self):
        return self.res

    def sendSeeOther(self, location):
        self.response = redirect(
            ''.join((request.url_root, 'chellow', location)), 303)

    def getFileItem(self, name):
        return ChellowFileItem(self.request.files[name])


@app.route('/chellow/reports/<int:report_id>/output/', methods=['GET', 'POST'])
def show_report(report_id):
    report = Report.query.get(report_id)
    inv = Invocation(request, g.user)
    try:
        exec(report.script, {'inv': inv, 'template': report.template})
        return inv.response
    except:
        return Response(traceback.format_exc(), status=500)


@app.route('/chellow/reports/', methods=['GET'])
def show_reports():
    return 'Hello Worlds'

@app.route('/chellow/reports/', methods=['POST'])
def add_report():
    set_read_write()
    is_core = POST_bool('is-core')
    name = POST_str("name")
    report = Report(None, is_core, name, "", None)
    db.session.add(report)
    try:
        db.session.commit()
    except ProgrammingError, e:
        if 'duplicate key value violates unique constraint' in str(e):
            return Response(
                "There's already a report with that name.", status=400)
        else:
            raise
    return redirect('/reports/' + str(report.id) + '/', 303)

@app.route('/chellow/reports/<int:report_id>/', methods=['GET'])
def show_edit_report(report_id):
    report = Report.query.get(report_id)
    render_template('report.html', {'report', report})
