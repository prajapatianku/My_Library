package com.example.ui.theme

fun translate(text: String, isHindi: Boolean): String {
    if (!isHindi) return text
    return when (text.trim()) {
        "Dashboard" -> "डैशबोर्ड"
        "Students" -> "छात्र"
        "Check-In" -> "चेक-इन"
        "Payments" -> "भुगतान"
        "Settings" -> "सेटिंग्स"
        "Active Students" -> "सक्रिय छात्र"
        "Vacant Seats" -> "खाली सीटें"
        "Active Plan" -> "सक्रिय प्लान"
        "Days Remaining" -> "शेष दिन"
        "Enroll Student" -> "छात्र पंजीकृत करें"
        "Enroll New Student" -> "नया छात्र जोड़ें"
        "Collect Fee" -> "फीस जमा करें"
        "Add Student" -> "छात्र जोड़ें"
        "Gender" -> "लिंग"
        "Gender *" -> "लिंग *"
        "Address" -> "पता"
        "Terms & Conditions" -> "नियम और शर्तें"
        "Language" -> "ऐप भाषा / Select Language"
        "Select Language" -> "भाषा चुनें"
        "English" -> "English"
        "Hindi" -> "हिन्दी (Hindi)"
        "Library Details & Timings" -> "लाइब्रेरी विवरण और समय"
        "Manage Library Shifts" -> "लाइब्रेरी शिफ्ट प्रबंधन"
        "WhatsApp Fee Due Reminders" -> "व्हाट्सएप फीस रिमाइंडर"
        "Multi-Branch Management" -> "बहु-शाखा प्रबंधन"
        "Student Registration QR Code" -> "रजिस्ट्रेशन क्यूआर कोड"
        "Audit & Security Logs" -> "ऑडिट एवं सुरक्षा लॉग"
        "Support & Helpdesk" -> "सहायता एवं हेल्पडेस्क"
        "Account & Session" -> "खाता और सत्र"
        "Logout" -> "लॉगआउट"
        "Library Operations" -> "लाइब्रेरी संचालन"
        else -> text
    }
}
