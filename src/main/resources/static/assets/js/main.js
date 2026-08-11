    /* ANTRA — vanilla interactions */
    (function () {
        "use strict";
        const reduce = window.matchMedia("(prefers-reduced-motion: reduce)").matches;
        const nav = document.getElementById("nav");
        const burger = document.getElementById("burger");
        const mobileMenu = document.getElementById("mobileMenu");

        /* ---- sticky nav ---- */
        function onScroll() {
            if (nav) nav.classList.toggle("scrolled", window.scrollY > 40);
            parallax();
        }
        window.addEventListener("scroll", onScroll, { passive: true });
        onScroll();

        /* ---- mobile menu ---- */
        function setMenu(open) {
            if (!mobileMenu || !burger) return;
            mobileMenu.classList.toggle("open", open);
            burger.classList.toggle("open", open);
            burger.setAttribute("aria-expanded", String(open));
            burger.setAttribute("aria-label", open ? "Close menu" : "Open menu");
            mobileMenu.setAttribute("aria-hidden", String(!open));
            document.body.classList.toggle("no-scroll", open);
        }
        if (burger) {
            burger.addEventListener("click", function () {
                setMenu(!mobileMenu.classList.contains("open"));
            });
        }
        if (mobileMenu) {
            mobileMenu.addEventListener("click", function (e) {
                if (e.target === mobileMenu) setMenu(false);
            });
        }
        document.addEventListener("keydown", function (e) {
            if (e.key === "Escape") setMenu(false);
        });

        /* ---- smooth anchor scroll with nav offset ---- */
        document.querySelectorAll('a[href^="#"]').forEach(function (link) {
            link.addEventListener("click", function (e) {
                var id = link.getAttribute("href");
                if (!id || id === "#") return;
                var target = document.querySelector(id);
                if (!target) return;
                e.preventDefault();
                setMenu(false);
                var offset = nav ? nav.offsetHeight : 0;
                var top = target.getBoundingClientRect().top + window.pageYOffset - offset + 1;
                window.scrollTo({ top: top, behavior: reduce ? "auto" : "smooth" });
                history.replaceState(null, "", id);
            });
        });

        /* ---- reveal on scroll ---- */
        var revealEls = document.querySelectorAll(".reveal,.reveal-left,.reveal-right,.reveal-up,.reveal-scale");
        if ("IntersectionObserver" in window) {
            var io = new IntersectionObserver(
                function (entries) {
                    entries.forEach(function (entry, i) {
                        if (!entry.isIntersecting) return;
                        var el = entry.target;
                        var delay = Number(el.dataset.delay || i * 90);
                        setTimeout(function () { el.classList.add("show"); }, delay);
                        io.unobserve(el);
                    });
                },
                { threshold: 0.15, rootMargin: "0px 0px -8% 0px" }
            );
            revealEls.forEach(function (el) { io.observe(el); });
        } else {
            revealEls.forEach(function (el) { el.classList.add("show"); });
        }

        /* ---- counters ---- */
        /* ===== COUNTERS ===== */

        var stats = document.getElementById("stats");

        function runCounters() {

            if (!stats) return;

            var counters = stats.querySelectorAll("[data-count]");

            counters.forEach(function (el) {

                var end = Number(el.dataset.count);
                var suffix = el.dataset.suffix || "";
                var duration = 1600;
                var start = performance.now();

                function step(now) {

                    var progress = Math.min(
                        (now - start) / duration,
                        1
                    );

                    /* Smooth ease-out */
                    var eased = 1 - Math.pow(1 - progress, 3);

                    var value = Math.round(end * eased);

                    el.textContent = value + suffix;

                    if (progress < 1) {
                        requestAnimationFrame(step);
                    }

                }

                requestAnimationFrame(step);

            });
        }


        /* ===== START WHEN VISIBLE ===== */

        if (stats && "IntersectionObserver" in window) {

            var statsObserver = new IntersectionObserver(
                function (entries) {

                    if (entries[0].isIntersecting) {

                        runCounters();

                        statsObserver.disconnect();

                    }

                },
                {
                    threshold: 0.3
                }
            );

            statsObserver.observe(stats);

        } else if (stats) {

            /* Fallback */
            runCounters();

        }
        /* ---- skill bars ---- */
        var bars = document.querySelectorAll("[data-bar]");
        if (bars.length && "IntersectionObserver" in window) {
            var bo = new IntersectionObserver(function (entries) {
                entries.forEach(function (entry) {
                    if (!entry.isIntersecting) return;
                    entry.target.style.width = entry.target.dataset.bar + "%";
                    bo.unobserve(entry.target);
                });
            }, { threshold: 0.4 });
            bars.forEach(function (b) { bo.observe(b); });
        }

        /* ---- projects slider (desktop transform / mobile native scroll) ---- */
        var track = document.getElementById("projTrack");
        var viewport = document.getElementById("projViewport");
        var projIndex = 0;
        function perView() {
            if (window.innerWidth < 768) return 1;
            return window.innerWidth < 1200 ? 2 : 3;
        }
        function maxIndex() {
            if (!track) return 0;
            return Math.max(0, track.children.length - perView());
        }
        function renderProjects() {
            if (!track || window.innerWidth < 768) return;
            projIndex = Math.min(projIndex, maxIndex());
            var card = track.children[0];
            var step = card.getBoundingClientRect().width + 22;
            track.style.transform = "translateX(" + -projIndex * step + "px)";
        }
        function move(dir) { projIndex = Math.min(Math.max(projIndex + dir, 0), maxIndex()); renderProjects(); }
        var pPrev = document.getElementById("projPrev");
        var pNext = document.getElementById("projNext");
        if (pPrev) pPrev.addEventListener("click", function () { move(-1); });
        if (pNext) pNext.addEventListener("click", function () { move(1); });
        window.addEventListener("resize", renderProjects);
        renderProjects();

        /* touch swipe on projects (desktop-style track) */
        if (viewport && track) {
            var sx = null;
            viewport.addEventListener("touchstart", function (e) { sx = e.touches[0].clientX; }, { passive: true });
            viewport.addEventListener("touchend", function (e) {
                if (sx === null || window.innerWidth < 768) return;
                var dx = e.changedTouches[0].clientX - sx;
                if (Math.abs(dx) > 50) move(dx < 0 ? 1 : -1);
                sx = null;
            }, { passive: true });
        }

        /* ---- testimonials slider ---- */
        var tTrack = document.getElementById("testTrack");
        var tSlider = document.getElementById("testSlider");
        if (tTrack && tSlider) {
            var ti = 0, total = tTrack.children.length, timer = null;
            function renderT() { tTrack.style.transform = "translateX(" + -ti * 100 + "%)"; }
            function go(dir) { ti = (ti + dir + total) % total; renderT(); }
            document.getElementById("testPrev").addEventListener("click", function () { go(-1); restart(); });
            document.getElementById("testNext").addEventListener("click", function () { go(1); restart(); });
            function start() { if (!reduce) timer = setInterval(function () { go(1); }, 5500); }
            function stop() { clearInterval(timer); }
            function restart() { stop(); start(); }
            tSlider.addEventListener("mouseenter", stop);
            tSlider.addEventListener("mouseleave", start);
            var tsx = null;
            tSlider.addEventListener("touchstart", function (e) { tsx = e.touches[0].clientX; stop(); }, { passive: true });
            tSlider.addEventListener("touchend", function (e) {
                if (tsx === null) return;
                var dx = e.changedTouches[0].clientX - tsx;
                if (Math.abs(dx) > 45) go(dx < 0 ? 1 : -1);
                tsx = null; start();
            }, { passive: true });
            renderT(); start();
        }

        /* ---- subtle parallax ---- */
        var pxEls = Array.prototype.slice.call(document.querySelectorAll("[data-parallax]"));
        function parallax() {
            if (reduce || window.innerWidth < 768 || !pxEls || !pxEls.length) return;
            var vh = window.innerHeight;
            pxEls.forEach(function (el) {
                var box = el.parentElement.getBoundingClientRect();
                if (box.bottom < 0 || box.top > vh) return;
                var progress = (box.top + box.height / 2 - vh / 2) / vh;
                el.style.transform = "translate3d(0," + (progress * -28).toFixed(2) + "px,0)";
            });
        }

        /* ---- video CTA ---- */
        var playBtn = document.getElementById("playBtn");
        if (playBtn) {
            playBtn.addEventListener("click", function () {
                playBtn.setAttribute("aria-label", "Studio film coming soon");
                playBtn.textContent = "\u2713";
                setTimeout(function () { playBtn.innerHTML = "&#9654;"; playBtn.setAttribute("aria-label", "Play studio film"); }, 1800);
            });
        }

        /* ---- contact form ---- */
        var form = document.getElementById("contactForm");
        var msg = document.getElementById("formMsg");
        if (form && msg) {
            form.addEventListener("submit", function (e) {
                e.preventDefault();
                var data = new FormData(form);
                var name = String(data.get("name") || "").trim();
                var email = String(data.get("email") || "").trim();
                if (!name || !/^\S+@\S+\.\S+$/.test(email)) {
                    msg.textContent = "Please add your name and a valid email address.";
                    return;
                }
                msg.textContent = "Thank you, " + name + ". We'll be in touch within one business day.";
                form.reset();
            });
        }


        /* ---- active nav link ---- */
        var sections = ["home", "about", "projects", "services", "process", "contact"]
            .map(function (id) { return document.getElementById(id); })
            .filter(Boolean);
        if (sections.length && "IntersectionObserver" in window) {
            var links = document.querySelectorAll(".nav-links a");
            var ao = new IntersectionObserver(function (entries) {
                entries.forEach(function (entry) {
                    if (!entry.isIntersecting) return;
                    links.forEach(function (l) {
                        l.classList.toggle("active", l.getAttribute("href") === "#" + entry.target.id);
                    });
                });
            }, { threshold: 0.4 });
            sections.forEach(function (s) { ao.observe(s); });
        }
    })();
    // =====================================================
    // CONTACT FORM
    // =====================================================

    document.addEventListener("DOMContentLoaded", function () {

        const contactForm =
            document.getElementById("contactForm");

        const formMsg =
            document.getElementById("formMsg");


        // -------------------------------------------------
        // FORM NOT FOUND
        // -------------------------------------------------

        if (!contactForm) {
            return;
        }


        if (!formMsg) {

            console.error(
                "Contact form message element #formMsg not found."
            );

            return;
        }


        // -------------------------------------------------
        // CLEAR ANY OLD MESSAGE
        // -------------------------------------------------

        formMsg.textContent = "";

        formMsg.innerHTML = "";

        formMsg.className = "form-msg";


        // -------------------------------------------------
        // SUBMIT
        // -------------------------------------------------

        contactForm.addEventListener(
            "submit",
            async function (event) {

                event.preventDefault();

                event.stopPropagation();


                // ---------------------------------------------
                // CLEAR OLD MESSAGE
                // ---------------------------------------------

                formMsg.textContent = "";

                formMsg.innerHTML = "";

                formMsg.className = "form-msg";


                // ---------------------------------------------
                // GET INPUTS
                // ---------------------------------------------

                const nameInput =
                    contactForm.querySelector(
                        '[name="name"]'
                    );


                const emailInput =
                    contactForm.querySelector(
                        '[name="email"]'
                    );


                const phoneInput =
                    contactForm.querySelector(
                        '[name="phone"]'
                    );


                const typeInput =
                    contactForm.querySelector(
                        '[name="type"]'
                    );


                const messageInput =
                    contactForm.querySelector(
                        '[name="message"]'
                    );


                // ---------------------------------------------
                // SAFETY CHECK
                // ---------------------------------------------

                if (
                    !nameInput ||
                    !emailInput ||
                    !phoneInput ||
                    !typeInput ||
                    !messageInput
                ) {

                    console.error(
                        "One or more contact form fields are missing."
                    );

                    return;
                }


                // ---------------------------------------------
                // VALUES
                // ---------------------------------------------

                const name =
                    nameInput.value.trim();


                const email =
                    emailInput.value.trim();


                const phone =
                    phoneInput.value.trim();


                const type =
                    typeInput.value.trim();


                const message =
                    messageInput.value.trim();


                // ---------------------------------------------
                // CUSTOM VALIDATION
                // ---------------------------------------------

                if (name === "") {

                    showFormError(
                        "Please enter your name."
                    );

                    nameInput.focus();

                    return;
                }


                if (email === "") {

                    showFormError(
                        "Please enter your email address."
                    );

                    emailInput.focus();

                    return;
                }


                const emailPattern =
                    /^[^\s@]+@[^\s@]+\.[^\s@]+$/;


                if (
                    !emailPattern.test(email)
                ) {

                    showFormError(
                        "Please enter a valid email address."
                    );

                    emailInput.focus();

                    return;
                }


                // ---------------------------------------------
                // SUBMIT BUTTON
                // ---------------------------------------------

                const submitButton =
                    contactForm.querySelector(
                        'button[type="submit"]'
                    );


                if (!submitButton) {

                    console.error(
                        "Submit button not found."
                    );

                    return;
                }


                const originalButtonText =
                    submitButton.textContent;


                submitButton.disabled = true;

                submitButton.textContent =
                    "Sending...";


                // ---------------------------------------------
                // REQUEST
                // ---------------------------------------------

                const data = {

                    name: name,

                    email: email,

                    phone: phone,

                    type: type,

                    message: message
                };


                // ---------------------------------------------
                // SEND TO SPRING BOOT
                // ---------------------------------------------

                try {

                    const response =
                        await fetch(
                            "/api/contact",
                            {
                                method: "POST",

                                headers: {
                                    "Content-Type":
                                        "application/json"
                                },

                                credentials:
                                    "same-origin",

                                body:
                                    JSON.stringify(data)
                            }
                        );


                    let result = null;


                    try {

                        result =
                            await response.json();

                    } catch (jsonError) {

                        console.error(
                            "Invalid server response.",
                            jsonError
                        );
                    }


                    // -----------------------------------------
                    // SERVER ERROR
                    // -----------------------------------------

                    if (!response.ok) {

                        throw new Error(
                            result &&
                            result.message
                                ? result.message
                                : "Unable to submit your enquiry."
                        );
                    }


                    if (
                        result &&
                        result.success === false
                    ) {

                        throw new Error(
                            result.message ||
                            "Unable to submit your enquiry."
                        );
                    }


                    // -----------------------------------------
                    // SUCCESS
                    // -----------------------------------------

                    formMsg.textContent =
                        "Thank you! We'll be in touch within one business day.";

                    formMsg.className =
                        "form-msg success";


                    // Clear form
                    contactForm.reset();


                } catch (error) {

                    console.error(
                        "Contact form error:",
                        error
                    );


                    formMsg.textContent =
                        error.message ||
                        "Something went wrong. Please try again.";

                    formMsg.className =
                        "form-msg error";


                } finally {

                    submitButton.disabled = false;

                    submitButton.textContent =
                        originalButtonText;
                }

            }
        );


        // =================================================
        // CUSTOM ERROR MESSAGE
        // =================================================

        function showFormError(message) {

            formMsg.textContent = message;

            formMsg.className =
                "form-msg error";
        }

    });