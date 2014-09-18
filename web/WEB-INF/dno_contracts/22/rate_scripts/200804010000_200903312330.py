def lafs():
    return {
        'hv': {
            'winter-weekday-peak': 1.058,
            'winter-weekday-day': 1.051,
            'night': 1.041,
            'other': 1.046},
        'lv-sub': {
            'winter-weekday-peak': 1.070,
            'winter-weekday-day': 1.066,
            'night': 1.060,
            'other': 1.062},
        'lv-net': {
            'winter-weekday-peak': 1.079,
            'winter-weekday-day': 1.073,
            'night': 1.067,
            'other': 1.069}}

def tariffs():
    return {
        '510': {
            'gbp-per-kva-per-day': 0.0378,
            'excess-gbp-per-kva-per-day': 0.0378,
            'day-gbp-per-kwh': 0.0039,
            'night-gbp-per-kwh': 0.0012,
            'reactive-gbp-per-kvarh': 0.0023},
        '520': {
            'gbp-per-kva-per-day': 0.0378,
            'excess-gbp-per-kva-per-day': 0.0378,
            'day-gbp-per-kwh': 0.0039,
            'night-gbp-per-kwh': 0.0012,
            'reactive-gbp-per-kvarh': 0.0023},
        '521': {
            'gbp-per-kva-per-day': 0.0174,
            'excess-gbp-per-kva-per-day': 0.0174,
            'day-gbp-per-kwh': 0,
            'night-gbp-per-kwh': 0},
        '540': {
            'gbp-per-kva-per-day': 0.0368,
            'excess-gbp-per-kva-per-day': 0.0368,
            'day-gbp-per-kwh': 0.0073,
            'night-gbp-per-kwh': 0.0026,
            'reactive-gbp-per-kvarh': 0.0033},
        '550': {
            'gbp-per-kva-per-day': 0.0368,
            'excess-gbp-per-kva-per-day': 0.0368,
            'day-gbp-per-kwh': 0.0073,
            'night-gbp-per-kwh': 0.0026,
            'reactive-gbp-per-kvarh': 0.0033},
        '570': {
            'gbp-per-kva-per-day': 0.0358,
            'excess-gbp-per-kva-per-day': 0.0358,
            'day-gbp-per-kwh': 0.0115,
            'night-gbp-per-kwh': 0.0038,
            'reactive-gbp-per-kvarh': 0.0042},
        '580': {
            'gbp-per-kva-per-day': 0.0358,
            'excess-gbp-per-kva-per-day': 0.0358,
            'day-gbp-per-kwh': 0.0115,
            'night-gbp-per-kwh': 0.0038,
            'reactive-gbp-per-kvarh': 0.0042},
        '581': {
            'gbp-per-kva-per-day': 0.0167,
            'excess-gbp-per-kva-per-day': 0.0167,
            'day-gbp-per-kwh': 0,
            'night-gbp-per-kwh': 0}}