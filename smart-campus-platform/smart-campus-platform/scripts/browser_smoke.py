"""Optional browser smoke test: pip install playwright; playwright install chromium.
Run the seeded backend and Vite preview first. Never run against production data.
BASE_URL defaults to http://localhost:4173. No data is created or changed.
"""
import json
import os
import re
from pathlib import Path
from playwright.sync_api import sync_playwright

base = os.getenv('BASE_URL', 'http://localhost:4173').rstrip('/')
routes_file = Path(__file__).resolve().parents[1] / 'frontend/src/routes/AppRoutes.jsx'
routes = re.findall(r'path="(/(?:admin|faculty|student)/[^"]+)"', routes_file.read_text())
results, failures = [], []
with sync_playwright() as p:
    browser = p.chromium.launch(headless=True, args=['--no-sandbox'])
    for role in ['admin', 'faculty', 'student']:
        context = browser.new_context()
        page = context.new_page()
        current = [f'{role} login']
        page.on('pageerror', lambda error: failures.append({'page': current[0], 'javascript': str(error)}))
        page.on('response', lambda response: failures.append({'page': current[0], 'url': response.url, 'status': response.status}) if '/api/' in response.url and response.status >= 400 else None)
        page.on('requestfailed', lambda request: failures.append({'page': current[0], 'request': request.url, 'error': request.failure}) if '/api/' in request.url and 'ERR_ABORTED' not in str(request.failure) else None)
        page.goto(base + '/login')
        page.locator('input[name="email"]').fill(f'{role}@smartcampus.com')
        page.locator('input[name="password"]').fill('Demo@1234')
        page.get_by_role('button', name='Sign in', exact=True).click()
        page.wait_for_url(f'**/{role}/dashboard')
        for route in [r for r in routes if r.startswith('/' + role + '/')]:
            current[0] = route
            page.goto(base + route)
            page.wait_for_load_state('networkidle')
            assert page.url.endswith(route), f'Redirected away from {route}: {page.url}'
            assert len(page.locator('main').inner_text().strip()) > 10, f'Empty page: {route}'
            results.append(route)
            if route in ['/admin/students','/admin/faculty','/admin/subjects','/admin/classrooms']:
                search = page.locator('input[placeholder*="Search" i]').first
                if search.count():
                    for term in ['a','A','zzzz-no-match','']:
                        search.fill(term)
                        page.wait_for_timeout(400)
                        page.wait_for_load_state('networkidle')
        context.close()
    browser.close()
print(json.dumps({'pages_checked': len(results), 'routes': results, 'failures': failures}, indent=2))
raise SystemExit(1 if failures else 0)
