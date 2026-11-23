# PUP - BUS SYSTEM

A native Android application built in **Kotlin** to centralize campus bus transit, schedules, and student-admin communication for **Punjabi University, Patiala (PUP)**.

---

## Features

### Commuter (Student) Features
- **Route Timetable:** Morning arrivals (`7:30 AM`, `8:30 AM`) and afternoon return departures (`12:50 PM`, `2:50 PM`, `3:50 PM`, `4:50 PM`).
- **Campus & City Stops:** Coverage of campus pickup points (PUP Library, Dispensary, Hostels Road, Main Gate) and city stops (New Bus Stand, Old Bus Stand, Lakad Mandi, Gurudwara Dukhniwaran Sahib, YPS Road, Mohindra College) with travel time estimates.
- **Fleet & Driver Directory:** Bus numbers and direct driver contact details.
- **Semester Bus Pass Details:** Pass fee breakdown (`Rs. 1,180`), boarding stations, and required HOD/Registrar Office approval steps.
- **In-App Help Center:** Submit support tickets for route or service inquiries.

### Admin Features
- **Real-Time Student Directory:** Search registered students by name or roll number.
- **Query Management:** Review and reply to student support tickets.
- **Emergency Popup Alerts:** Broadcast instant campus transit notices (such as route suspensions or schedule changes).

---

## Tech Stack & Architecture
- **Language:** Kotlin
- **UI & Android Jetpack:** XML Layouts, Material Components, ConstraintLayout, Coroutines, DataStore
- **Backend & Auth:** Firebase Authentication, Cloud Firestore
- **Firestore Collections:**
  - `students` — student profile, roll number, department, and course details
  - `notices` — active campus transit alerts and timestamps
  - `queries` — student help tickets and admin replies

---

## Academic Context
Developed as the **MCAM1206L Minor Project** (MCA, Department of Computer Science, Punjabi University, Patiala).

---

## Author
**Amanpreet** — [@Amanpreet0320](https://github.com/Amanpreet0320)
