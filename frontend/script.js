(function () {
    "use strict";

    const apiBase = "http://localhost:8080";

    async function post(path, values) {
        const response = await fetch(apiBase + path, {
            method: "POST",
            headers: {
                "Content-Type": "application/x-www-form-urlencoded;charset=UTF-8"
            },
            body: new URLSearchParams(values)
        });
        const result = await response.json();
        if (!response.ok) {
            throw new Error(result.error || "The request could not be completed.");
        }
        return result;
    }

    function showMessage(element, message, isError) {
        element.textContent = message;
        element.classList.toggle("error-message", Boolean(isError));
        element.classList.toggle("success-message", !isError);
        element.hidden = false;
    }

    function addStatusRow(container, label, value) {
        const row = document.createElement("div");
        row.className = "row";
        const caption = document.createElement("span");
        caption.textContent = label;
        const content = document.createElement("strong");
        content.textContent = value || "—";
        row.append(caption, content);
        container.append(row);
    }

    function initializeStatusForm() {
        const form = document.getElementById("statusForm");
        if (!form) {
            return;
        }

        const result = document.getElementById("result");
        form.addEventListener("submit", async function (event) {
            event.preventDefault();
            result.replaceChildren();
            result.hidden = false;
            try {
                const application = await post("/status", {
                    applicationNumber: document.getElementById("applicationNumber").value.trim(),
                    dob: document.getElementById("dob").value
                });
                const card = document.createElement("div");
                card.className = "status-card";
                const heading = document.createElement("h3");
                heading.textContent = "Application Details";
                card.append(heading);
                addStatusRow(card, "Application Number", application.applicationNumber);
                addStatusRow(card, "Candidate Name", application.candidateName);
                addStatusRow(card, "Date of Birth", application.dob);
                addStatusRow(card, "Examination", application.examName);
                addStatusRow(card, "Exam Level", application.examLevel);
                addStatusRow(card, "Exam Centre", application.examCenter);
                addStatusRow(card, "Application Date", application.applicationDate);
                addStatusRow(card, "Application Status", application.status);
                result.append(card);
            } catch (error) {
                const message = document.createElement("div");
                message.className = "error";
                message.textContent = error.message
                    || "Cannot connect to the Java server. Make sure the API is running.";
                result.append(message);
            }
        });
    }

    function initializeLoginForm() {
        const form = document.getElementById("loginForm");
        if (!form) {
            return;
        }
        const message = document.getElementById("loginMessage");
        form.addEventListener("submit", async function (event) {
            event.preventDefault();
            message.hidden = true;
            try {
                const result = await post("/login", {
                    email: document.getElementById("email").value.trim(),
                    password: document.getElementById("password").value
                });
                localStorage.setItem("examRegistrationToken", result.token);
                window.location.href = "dashboard.html";
            } catch (error) {
                showMessage(message, error.message
                    || "Cannot connect to the Java server. Make sure the API is running.", true);
            }
        });
    }

    async function initializeDashboard() {
        const dashboard = document.getElementById("dashboardContent");
        if (!dashboard) {
            return;
        }
        const token = localStorage.getItem("examRegistrationToken");
        if (!token) {
            window.location.replace("login.html");
            return;
        }
        try {
            const data = await post("/dashboard", { token: token });
            document.getElementById("studentName").textContent = data.student.name;
            document.getElementById("studentEmail").textContent = data.student.email;
            document.getElementById("studentMobile").textContent = data.student.mobile;
            document.getElementById("studentQualification").textContent =
                [data.student.qualification, data.student.course].filter(Boolean).join(" · ") || "—";

            const applications = document.getElementById("applicationList");
            applications.replaceChildren();
            if (data.applications.length === 0) {
                const empty = document.createElement("p");
                empty.textContent = "You have not submitted an exam application yet.";
                applications.append(empty);
                return;
            }
            data.applications.forEach(function (application) {
                const card = document.createElement("article");
                card.className = "card application-card";
                const heading = document.createElement("h2");
                heading.textContent = application.examName;
                card.append(heading);
                addStatusRow(card, "Application Number", application.applicationNumber);
                addStatusRow(card, "Level", application.examLevel);
                addStatusRow(card, "Centre", application.examCenter);
                addStatusRow(card, "Submitted", application.applicationDate);
                addStatusRow(card, "Status", application.status);
                const link = document.createElement("a");
                link.className = "main-button";
                link.href = "status.html";
                link.textContent = "Check application status";
                card.append(link);
                applications.append(card);
            });
        } catch (error) {
            localStorage.removeItem("examRegistrationToken");
            showMessage(document.getElementById("dashboardMessage"),
                error.message || "Could not load your dashboard.", true);
            if (error.message && error.message.includes("session")) {
                window.location.replace("login.html");
            }
        }
    }

    function initializeLogout() {
        document.querySelectorAll("[data-logout]").forEach(function (link) {
            link.addEventListener("click", async function (event) {
                event.preventDefault();
                const token = localStorage.getItem("examRegistrationToken");
                try {
                    if (token) {
                        await post("/logout", { token: token });
                    }
                } catch (error) {
                    console.error("Could not invalidate the server session:", error);
                } finally {
                    localStorage.removeItem("examRegistrationToken");
                    window.location.href = "index.html";
                }
            });
        });
    }

    function initializeExamSelection() {
        const exam = new URLSearchParams(window.location.search).get("exam");
        const examSelect = document.getElementById("examName");
        if (exam && examSelect
                && Array.from(examSelect.options).some(function (option) {
                    return option.value === exam;
                })) {
            examSelect.value = exam;
        }
    }

    window.ExamRegistration = { post: post, showMessage: showMessage };

    document.addEventListener("DOMContentLoaded", function () {
        initializeStatusForm();
        initializeLoginForm();
        initializeDashboard();
        initializeLogout();
        initializeExamSelection();
    });
})();
