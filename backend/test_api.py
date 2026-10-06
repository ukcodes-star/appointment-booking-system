import urllib.request
import urllib.error
import json
import uuid
import datetime

BASE_URL = "http://localhost:8080"

def request(method, path, body=None, token=None):
    url = BASE_URL + path
    headers = {"Content-Type": "application/json"}
    if token:
        headers["Authorization"] = f"Bearer {token}"
    data = json.dumps(body).encode("utf-8") if body is not None else None
    req = urllib.request.Request(url, data=data, headers=headers, method=method)
    try:
        with urllib.request.urlopen(req) as resp:
            content = resp.read().decode("utf-8")
            res_json = json.loads(content) if content else {}
            return resp.status, res_json
    except urllib.error.HTTPError as e:
        content = e.read().decode("utf-8")
        try:
            res_json = json.loads(content)
        except Exception:
            res_json = {"raw": content}
        return e.code, res_json
    except Exception as e:
        return 500, {"error": str(e)}

results = []

def run_test(name, expected_status, method, path, body=None, token=None):
    status, data = request(method, path, body, token)
    passed = (status == expected_status)
    status_icon = "PASS [OK]" if passed else f"FAIL [Expected {expected_status}, Got {status}]"
    results.append({
        "test": name,
        "expected": expected_status,
        "actual": status,
        "passed": passed,
        "data": data
    })
    print(f"{name:.<65} {status_icon}")
    return status, data

print("\n========================================================")
print("     APPOINTMENT BOOKING SYSTEM - API TEST SUITE        ")
print("========================================================\n")

unique_suffix = str(uuid.uuid4())[:8]
doc_email = f"doctor_{unique_suffix}@clinic.com"
user_email = f"patient_{unique_suffix}@gmail.com"
doc2_email = f"doctor2_{unique_suffix}@clinic.com"
password = "Password@123"

# 1. Auth Tests
run_test("1. Register Provider (Success 201)", 201, "POST", "/auth/register", {
    "name": "Dr. Sarah Smith",
    "email": doc_email,
    "password": password,
    "role": "SERVICE_PROVIDER"
})

run_test("2. Register Duplicate Email (409 Conflict)", 409, "POST", "/auth/register", {
    "name": "Dr. Sarah Smith",
    "email": doc_email,
    "password": password,
    "role": "SERVICE_PROVIDER"
})

run_test("3. Register Regular User (Success 201)", 201, "POST", "/auth/register", {
    "name": "John Doe",
    "email": user_email,
    "password": password,
    "role": "USER"
})

run_test("4. Register Other Provider (Success 201)", 201, "POST", "/auth/register", {
    "name": "Dr. Alan Grant",
    "email": doc2_email,
    "password": password,
    "role": "SERVICE_PROVIDER"
})

_, doc_login = run_test("5. Provider Login Success (200 OK + JWT)", 200, "POST", "/auth/login", {
    "email": doc_email,
    "password": password
})
doc_token = doc_login.get("token")

_, user_login = run_test("6. User Login Success (200 OK + JWT)", 200, "POST", "/auth/login", {
    "email": user_email,
    "password": password
})
user_token = user_login.get("token")

_, doc2_login = run_test("7. Other Provider Login Success (200 OK + JWT)", 200, "POST", "/auth/login", {
    "email": doc2_email,
    "password": password
})
doc2_token = doc2_login.get("token")

run_test("8. Login with Invalid Credentials (401 Unauthorized)", 401, "POST", "/auth/login", {
    "email": doc_email,
    "password": "WrongPassword!"
})

# 2. Service Creation Tests
_, svc_res = run_test("9. Create Service as Provider (Success 201)", 201, "POST", "/services", {
    "name": "Dental Checkup",
    "type": "MEDICAL",
    "durationMinutes": 30
}, token=doc_token)
service_id = svc_res.get("id")

run_test("10. Create Service with USER role (403 Forbidden)", 403, "POST", "/services", {
    "name": "Haircut",
    "type": "BEAUTY",
    "durationMinutes": 60
}, token=user_token)

run_test("11. Create Service invalid duration (45 mins != multiple of 30 -> 400)", 400, "POST", "/services", {
    "name": "Invalid Duration Service",
    "type": "MEDICAL",
    "durationMinutes": 45
}, token=doc_token)

run_test("12. Create Service duration out of range (150 mins > 120 -> 400)", 400, "POST", "/services", {
    "name": "Too Long Service",
    "type": "MEDICAL",
    "durationMinutes": 150
}, token=doc_token)

run_test("13. Create Service invalid enum type (400 Bad Request)", 400, "POST", "/services", {
    "name": "Bad Enum Service",
    "type": "NON_EXISTENT_TYPE",
    "durationMinutes": 30
}, token=doc_token)

# 3. Availability Tests
# Pick next Friday (DayOfWeek 5 in our convention: Sunday=0, Monday=1, ..., Friday=5, Saturday=6)
today = datetime.date.today()
# Find upcoming Friday
days_ahead = (5 - ((today.weekday() + 1) % 7)) % 7
if days_ahead == 0:
    days_ahead = 7
target_date = today + datetime.timedelta(days=days_ahead)
target_date_str = target_date.strftime("%Y-%m-%d")
day_of_week = 5

run_test("14. Set Availability as Provider (Success 201)", 201, "POST", f"/services/{service_id}/availability", {
    "dayOfWeek": day_of_week,
    "startTime": "09:00",
    "endTime": "12:00"
}, token=doc_token)

run_test("15. Set Overlapping Availability (409 Conflict)", 409, "POST", f"/services/{service_id}/availability", {
    "dayOfWeek": day_of_week,
    "startTime": "10:00",
    "endTime": "13:00"
}, token=doc_token)

run_test("16. Set Availability invalid minutes (09:15 != 00/30 -> 400)", 400, "POST", f"/services/{service_id}/availability", {
    "dayOfWeek": day_of_week,
    "startTime": "09:15",
    "endTime": "12:00"
}, token=doc_token)

run_test("17. Set Availability startTime >= endTime (12:00 to 09:00 -> 400)", 400, "POST", f"/services/{service_id}/availability", {
    "dayOfWeek": day_of_week,
    "startTime": "12:00",
    "endTime": "09:00"
}, token=doc_token)

run_test("18. Set Availability by wrong provider (403 Forbidden)", 403, "POST", f"/services/{service_id}/availability", {
    "dayOfWeek": day_of_week,
    "startTime": "14:00",
    "endTime": "16:00"
}, token=doc2_token)

run_test("19. Set Availability with USER role (403 Forbidden)", 403, "POST", f"/services/{service_id}/availability", {
    "dayOfWeek": day_of_week,
    "startTime": "14:00",
    "endTime": "16:00"
}, token=user_token)

# 4. Service Discovery & Slot Derivation Tests
run_test("20. Get All Services (200 OK)", 200, "GET", "/services")

run_test("21. Get Services Filtered by type=MEDICAL (200 OK)", 200, "GET", "/services?type=MEDICAL")

run_test("22. Get Services with Invalid Type (400 Bad Request)", 400, "GET", "/services?type=INVALID_TYPE")

_, slots_res = run_test("23. Get Slots for Service on Friday (200 OK)", 200, "GET", f"/services/{service_id}/slots?date={target_date_str}")
slots_list = slots_res.get("slots", [])
print(f"    --> Total dynamic slots generated: {len(slots_list)}")

run_test("24. Get Slots for Past Date (400 Bad Request)", 400, "GET", f"/services/{service_id}/slots?date=2020-01-01")

run_test("25. Get Slots for Non-Existent Service (404 Not Found)", 404, "GET", f"/services/{uuid.uuid4()}/slots?date={target_date_str}")

# 5. Appointment Booking Tests
booked_slot_id = slots_list[0].get("slotId") if slots_list else f"{service_id}_{target_date_str}_09:00"

_, appt_res = run_test("26. Book Appointment as User (Success 201)", 201, "POST", "/appointments", {
    "slotId": booked_slot_id
}, token=user_token)

run_test("27. Book Already Booked Slot (409 Conflict)", 409, "POST", "/appointments", {
    "slotId": booked_slot_id
}, token=user_token)

run_test("28. Book Appointment as Service Provider (403 Forbidden)", 403, "POST", "/appointments", {
    "slotId": booked_slot_id
}, token=doc_token)

run_test("29. Book Appointment Without Auth (401 Unauthorized)", 401, "POST", "/appointments", {
    "slotId": booked_slot_id
})

# 6. User Appointments Tests
_, my_appts = run_test("30. Get My Appointments as User (200 OK)", 200, "GET", "/appointments/me", token=user_token)
run_test("31. Get My Appointments Without Auth (401 Unauthorized)", 401, "GET", "/appointments/me")

# 7. Provider Daily Schedule Tests
_, prov_sched = run_test("32. Get Provider Daily Schedule (200 OK)", 200, "GET", f"/providers/me/schedule?date={target_date_str}", token=doc_token)
run_test("33. Get Provider Schedule with USER Role (403 Forbidden)", 403, "GET", f"/providers/me/schedule?date={target_date_str}", token=user_token)
run_test("34. Get Provider Schedule Invalid Date Format (400 Bad Request)", 400, "GET", "/providers/me/schedule?date=INVALID_DATE", token=doc_token)
run_test("35. Get Provider Schedule Without Auth (401 Unauthorized)", 401, "GET", f"/providers/me/schedule?date={target_date_str}")

# Summary
passed_count = sum(1 for r in results if r["passed"])
total_count = len(results)
print("\n========================================================")
print(f"             FINAL SUMMARY: {passed_count}/{total_count} PASSED")
print("========================================================\n")
if passed_count == total_count:
    print("ALL TEST CASES PASSED SUCCESSFULLY!")
else:
    print(f"FAILED TESTS: {total_count - passed_count}")
