/* =========================================================
   KSRTC BUS TIMETABLE FRONTEND
========================================================= */

document.addEventListener("DOMContentLoaded", function () {

    /* =====================================================
       ELEMENTS
    ===================================================== */

    const fromInput =
        document.getElementById("from");

    const toInput =
        document.getElementById("to");

    const serviceClass =
        document.getElementById("serviceClass");

    const searchBtn =
        document.getElementById("searchBtn");

    const swapBtn =
        document.getElementById("swapBtn");

    const fromSuggestions =
        document.getElementById("fromSuggestions");

    const toSuggestions =
        document.getElementById("toSuggestions");

    const results =
        document.getElementById("results");

    const searchStatus =
        document.getElementById("searchStatus");


    /* =====================================================
       PLACE LIST
    ===================================================== */

    const places = [

        "Bengaluru",
        "Mysuru",
        "Mandya",
        "Maddur",
        "Mangaluru",
        "Madikeri",
        "Madurai",
        "Mumbai",
        "Hassan",
        "Hosur",
        "Hubballi",
        "Dharwad",
        "Belagavi",
        "Ballari",
        "Tumakuru",
        "Ramanagara",
        "Channapatna",
        "Chikkaballapur",
        "Chikkamagaluru",
        "Shivamogga",
        "Davangere",
        "Kolar",
        "Kundapura",
        "Udupi",
        "Karwar",
        "Koppal",
        "Raichur",
        "Kalaburagi",
        "Bidar",
        "Vijayapura",
        "Sakleshpur",
        "Virajpet",
        "Somwarpet",
        "Srirangapatna",
        "Nanjangud",
        "Hunsur",
        "Kushalnagar",
        "Chamarajanagar",
        "Hampi",
        "Hospet",
        "KRS",
        "Brindavan Gardens",
        "PES",
        "Bannur",
        "Malavalli",
        "Pandavapura",
        "Nagamangala",
        "Krishnarajpet",
        "Tiptur",
        "Arsikere",
        "Chitradurga",
        "Gadag",
        "Yadgir",
        "Kengeri",
        "Electronic City",
        "Whitefield",
        "Kengal",
        "Bidadi",
        "Nelamangala",
        "Doddaballapura"

    ];


    /* =====================================================
       REMOVE DUPLICATES
    ===================================================== */

    const uniquePlaces =
        [...new Set(places)]
            .sort((a, b) =>
                a.localeCompare(b)
            );


    /* =====================================================
       AUTOCOMPLETE STATE
       
       We keep a separate highlighted index for
       From and To fields.
    ===================================================== */

    let fromSelectedIndex = -1;
    let toSelectedIndex = -1;


    /* =====================================================
       NORMALIZE TEXT
    ===================================================== */

    function normalize(value) {

        if (
            value === null ||
            value === undefined
        ) {
            return "";
        }

        return value
            .toString()
            .trim()
            .replace(/\s+/g, " ")
            .toLowerCase();
    }


    /* =====================================================
       SHOW SUGGESTIONS
    ===================================================== */

    function showSuggestions(input, container) {

        const value =
            normalize(input.value);

        container.innerHTML = "";

        /* Reset keyboard selection */
        if (input === fromInput) {
            fromSelectedIndex = -1;
        } else {
            toSelectedIndex = -1;
        }


        /* Empty input */
        if (value.length === 0) {

            container.classList.remove("show");

            return;
        }


        /* Find matching places */
        const matches =
            uniquePlaces.filter(place =>
                normalize(place)
                    .startsWith(value)
            );


        /* No matches */
        if (matches.length === 0) {

            container.classList.remove("show");

            return;
        }


        /* Create suggestion items */
        matches.forEach(function (place, index) {

            const item =
                document.createElement("div");

            item.className =
                "suggestion-item";

            /* Store index for keyboard navigation */
            item.dataset.index = index;


            const icon =
                document.createElement("span");

            icon.className =
                "suggestion-icon";

            icon.textContent = "📍";


            const text =
                document.createElement("span");

            text.textContent = place;


            item.appendChild(icon);
            item.appendChild(text);


            /* -------------------------------------------------
               MOUSE SELECTION
            ------------------------------------------------- */

            item.addEventListener(
                "mousedown",
                function (event) {

                    event.preventDefault();

                    selectSuggestion(
                        input,
                        container,
                        place
                    );

                }
            );


            container.appendChild(item);

        });


        container.classList.add("show");

    }


    /* =====================================================
       SELECT SUGGESTION
    ===================================================== */

    function selectSuggestion(
        input,
        container,
        place
    ) {

        input.value = place;

        container.innerHTML = "";

        container.classList.remove("show");


        if (input === fromInput) {

            fromSelectedIndex = -1;

        } else {

            toSelectedIndex = -1;

        }

    }


    /* =====================================================
       HIGHLIGHT SUGGESTION
    ===================================================== */

    function highlightSuggestion(
        container,
        index
    ) {

        const items =
            container.querySelectorAll(
                ".suggestion-item"
            );


        if (!items.length) {
            return;
        }


        /* Remove previous highlight */
        items.forEach(function (item) {

            item.classList.remove(
                "suggestion-active"
            );

        });


        /* Keep index inside range */
        if (index < 0) {
            index = items.length - 1;
        }

        if (index >= items.length) {
            index = 0;
        }


        /* Add highlight */
        const selectedItem =
            items[index];

        selectedItem.classList.add(
            "suggestion-active"
        );


        /* Make sure selected option is visible */
        selectedItem.scrollIntoView({
            block: "nearest"
        });


        return index;

    }


    /* =====================================================
       KEYBOARD NAVIGATION
       
       ↑ = previous
       ↓ = next
       Enter = select
       Esc = close
    ===================================================== */

    function handleAutocompleteKeydown(
        event,
        input,
        container
    ) {

        const items =
            container.querySelectorAll(
                ".suggestion-item"
            );


        /* -------------------------------------------------
           If suggestions are not open, allow normal keys.
        ------------------------------------------------- */

        if (
            !container.classList.contains("show") ||
            items.length === 0
        ) {

            return;

        }


        /* -------------------------------------------------
           DOWN ARROW
        ------------------------------------------------- */

        if (event.key === "ArrowDown") {

            event.preventDefault();

            let currentIndex =
                input === fromInput
                    ? fromSelectedIndex
                    : toSelectedIndex;


            currentIndex++;


            if (
                currentIndex >=
                items.length
            ) {

                currentIndex = 0;

            }


            const newIndex =
                highlightSuggestion(
                    container,
                    currentIndex
                );


            if (input === fromInput) {

                fromSelectedIndex =
                    newIndex;

            } else {

                toSelectedIndex =
                    newIndex;

            }

            return;
        }


        /* -------------------------------------------------
           UP ARROW
        ------------------------------------------------- */

        if (event.key === "ArrowUp") {

            event.preventDefault();

            let currentIndex =
                input === fromInput
                    ? fromSelectedIndex
                    : toSelectedIndex;


            /*
             * If nothing is selected yet,
             * ArrowUp starts at the last item.
             */

            if (currentIndex === -1) {

                currentIndex =
                    items.length - 1;

            } else {

                currentIndex--;

            }


            if (currentIndex < 0) {

                currentIndex =
                    items.length - 1;

            }


            const newIndex =
                highlightSuggestion(
                    container,
                    currentIndex
                );


            if (input === fromInput) {

                fromSelectedIndex =
                    newIndex;

            } else {

                toSelectedIndex =
                    newIndex;

            }

            return;
        }


        /* -------------------------------------------------
           ENTER
        ------------------------------------------------- */

        if (event.key === "Enter") {

            const currentIndex =
                input === fromInput
                    ? fromSelectedIndex
                    : toSelectedIndex;


            /*
             * If an autocomplete item is highlighted,
             * select it instead of immediately searching.
             */

            if (
                currentIndex >= 0 &&
                currentIndex < items.length
            ) {

                event.preventDefault();

                const selectedItem =
                    items[currentIndex];


                /*
                 * Get the actual place text.
                 *
                 * The first span is the icon.
                 * The second span contains the place.
                 */

                const textElement =
                    selectedItem.querySelector(
                        "span:last-child"
                    );


                const place =
                    textElement
                        ? textElement.textContent
                        : selectedItem.textContent;


                selectSuggestion(
                    input,
                    container,
                    place
                );


                return;

            }


            /*
             * If nothing is highlighted,
             * keep the existing behavior:
             * Enter searches buses.
             */

            event.preventDefault();

            container.classList.remove("show");

            searchBuses();

            return;
        }


        /* -------------------------------------------------
           ESCAPE
        ------------------------------------------------- */

        if (event.key === "Escape") {

            event.preventDefault();

            container.classList.remove("show");

            if (input === fromInput) {

                fromSelectedIndex = -1;

            } else {

                toSelectedIndex = -1;

            }

        }

    }


    /* =====================================================
       FROM INPUT
    ===================================================== */

    fromInput.addEventListener(
        "input",
        function () {

            showSuggestions(
                fromInput,
                fromSuggestions
            );

        }
    );


    fromInput.addEventListener(
        "focus",
        function () {

            if (
                normalize(fromInput.value)
                    .length > 0
            ) {

                showSuggestions(
                    fromInput,
                    fromSuggestions
                );

            }

        }
    );


    fromInput.addEventListener(
        "keydown",
        function (event) {

            handleAutocompleteKeydown(
                event,
                fromInput,
                fromSuggestions
            );

        }
    );


    /* =====================================================
       TO INPUT
    ===================================================== */

    toInput.addEventListener(
        "input",
        function () {

            showSuggestions(
                toInput,
                toSuggestions
            );

        }
    );


    toInput.addEventListener(
        "focus",
        function () {

            if (
                normalize(toInput.value)
                    .length > 0
            ) {

                showSuggestions(
                    toInput,
                    toSuggestions
                );

            }

        }
    );


    toInput.addEventListener(
        "keydown",
        function (event) {

            handleAutocompleteKeydown(
                event,
                toInput,
                toSuggestions
            );

        }
    );


    /* =====================================================
       CLOSE SUGGESTIONS WHEN CLICKING OUTSIDE
    ===================================================== */

    document.addEventListener(
        "click",
        function (event) {

            if (
                !fromInput.contains(event.target) &&
                !fromSuggestions.contains(event.target)
            ) {

                fromSuggestions.classList.remove(
                    "show"
                );

                fromSelectedIndex = -1;

            }


            if (
                !toInput.contains(event.target) &&
                !toSuggestions.contains(event.target)
            ) {

                toSuggestions.classList.remove(
                    "show"
                );

                toSelectedIndex = -1;

            }

        }
    );


    /* =====================================================
       SWAP
    ===================================================== */

    swapBtn.addEventListener(
        "click",
        function () {

            const temp =
                fromInput.value;

            fromInput.value =
                toInput.value;

            toInput.value =
                temp;


            fromSuggestions.classList.remove(
                "show"
            );

            toSuggestions.classList.remove(
                "show"
            );


            fromSelectedIndex = -1;
            toSelectedIndex = -1;

        }
    );


    /* =====================================================
       SEARCH BUTTON
    ===================================================== */

    searchBtn.addEventListener(
        "click",
        function () {

            searchBuses();

        }
    );


    /* =====================================================
       SEARCH FUNCTION
    ===================================================== */

    async function searchBuses() {

        const from =
            fromInput.value.trim();

        const to =
            toInput.value.trim();

        const selectedService =
            serviceClass.value.trim();


        /* -------------------------------------------------
           VALIDATION
        ------------------------------------------------- */

        if (!from) {

            showStatus(
                "Please enter a starting point.",
                "error"
            );

            fromInput.focus();

            return;
        }


        if (!to) {

            showStatus(
                "Please enter a destination.",
                "error"
            );

            toInput.focus();

            return;
        }


        if (
            normalize(from) ===
            normalize(to)
        ) {

            showStatus(
                "Starting point and destination cannot be the same.",
                "error"
            );

            return;
        }


        /* -------------------------------------------------
           CLOSE DROPDOWNS
        ------------------------------------------------- */

        fromSuggestions.classList.remove(
            "show"
        );

        toSuggestions.classList.remove(
            "show"
        );

        fromSelectedIndex = -1;
        toSelectedIndex = -1;


        /* -------------------------------------------------
           CLEAR OLD RESULTS
        ------------------------------------------------- */

        results.innerHTML = "";


        /* -------------------------------------------------
           LOADING STATE
        ------------------------------------------------- */

        searchBtn.disabled = true;

        searchBtn.innerHTML =
            "<span>Searching...</span><span>⏳</span>";


        showStatus(
            "Searching available bus services...",
            "loading"
        );


        /* -------------------------------------------------
           REQUEST BODY
        ------------------------------------------------- */

        const requestBody = {

            from: from,

            to: to,

            serviceClass:
                selectedService === ""
                    ? null
                    : selectedService

        };


        try {

            /* -------------------------------------------------
               BACKEND REQUEST
            ------------------------------------------------- */

            const response =
                await fetch(
                    "/api/buses/search",
                    {
                        method: "POST",

                        headers: {

                            "Content-Type":
                                "application/json",

                            "Accept":
                                "application/json"

                        },

                        body:
                            JSON.stringify(
                                requestBody
                            )

                    }
                );


            /* -------------------------------------------------
               CHECK HTTP RESPONSE
            ------------------------------------------------- */

            if (!response.ok) {

                let errorMessage =
                    "Unable to search buses.";

                try {

                    const errorText =
                        await response.text();

                    if (errorText) {

                        errorMessage =
                            errorText;

                    }

                } catch (ignored) {

                    // Keep default message.

                }


                throw new Error(
                    "HTTP " +
                    response.status +
                    ": " +
                    errorMessage
                );

            }


            /* -------------------------------------------------
               READ JSON
            ------------------------------------------------- */

            const data =
                await response.json();


            console.log(
                "Bus search response:",
                data
            );


            /* -------------------------------------------------
               HANDLE NO RESULTS
            ------------------------------------------------- */

            if (
                !Array.isArray(data) ||
                data.length === 0
            ) {

                showStatus(
                    "No buses were found for this journey.",
                    "error"
                );


                results.innerHTML = `

                    <div class="no-results">

                        <h3>
                            No bus services found
                        </h3>

                        <p>
                            Try another From and To location
                            or select a different service type.
                        </p>

                    </div>

                `;

                return;

            }


            /* -------------------------------------------------
               SUCCESS
            ------------------------------------------------- */

            showStatus(

                data.length +
                " bus service" +
                (data.length === 1
                    ? ""
                    : "s") +
                " found.",

                "success"

            );


            renderResults(data);


            /* Bring results into view */

            setTimeout(
                function () {

                    results.scrollIntoView({

                        behavior: "smooth",

                        block: "start"

                    });

                },
                100
            );

        }

        catch (error) {

            console.error(
                "Bus search failed:",
                error
            );


            showStatus(
                "Unable to retrieve bus information. " +
                "Please make sure the Spring Boot server is running.",
                "error"
            );


            results.innerHTML = `

                <div class="no-results">

                    <h3>
                        Search could not be completed
                    </h3>

                    <p>
                        The application could not connect to
                        the bus search service.
                    </p>

                    <p style="margin-top:10px;font-size:12px;">
                        Check the Spring Boot console for errors.
                    </p>

                </div>

            `;

        }

        finally {

            searchBtn.disabled = false;

            searchBtn.innerHTML =
                "<span>Search Buses</span>" +
                "<span class=\"button-arrow\">→</span>";

        }

    }


    /* =====================================================
       STATUS
    ===================================================== */

    function showStatus(
        message,
        type
    ) {

        searchStatus.textContent =
            message;

        searchStatus.className =
            "search-status " +
            (type || "");

    }


    /* =====================================================
       RENDER RESULTS
    ===================================================== */

    function renderResults(
        busList
    ) {

        results.innerHTML = "";


        const title =
            document.createElement("h3");

        title.className =
            "results-title";

        title.textContent =
            "Available Bus Services";


        results.appendChild(title);


        const grid =
            document.createElement("div");

        grid.className =
            "results-grid";


        busList.forEach(
            function (bus) {

                grid.appendChild(
                    createBusCard(bus)
                );

            }
        );


        results.appendChild(grid);

    }


    /* =====================================================
       CREATE BUS CARD
    ===================================================== */

    function createBusCard(
        bus
    ) {

        const card =
            document.createElement("article");

        card.className =
            "bus-card";


        const busTop =
            document.createElement("div");

        busTop.className =
            "bus-top";


        const service =
            document.createElement("div");

        service.className =
            "bus-service";

        service.textContent =
            bus.serviceClass ||
            "KSRTC Bus Service";


        const serviceType =
            document.createElement("span");

        serviceType.className =
            "service-class";

        serviceType.textContent =
            bus.serviceClass ||
            "Service";


        busTop.appendChild(service);

        busTop.appendChild(serviceType);


        /* -------------------------------------------------
           ROUTE
        ------------------------------------------------- */

        const route =
            document.createElement("div");

        route.className =
            "bus-route";


        const origin =
            document.createElement("div");

        origin.className =
            "route-place";

        origin.innerHTML = `

            <strong>
                ${escapeHtml(
                    bus.boardingPoint ||
                    bus.officialOrigin ||
                    "-"
                )}
            </strong>

            <small>
                Boarding
            </small>

        `;


        const arrow =
            document.createElement("div");

        arrow.className =
            "route-arrow";

        arrow.textContent =
            "→";


        const destination =
            document.createElement("div");

        destination.className =
            "route-place";

        destination.innerHTML = `

            <strong>
                ${escapeHtml(
                    bus.destination ||
                    bus.officialDestination ||
                    "-"
                )}
            </strong>

            <small>
                Destination
            </small>

        `;


        route.appendChild(origin);

        route.appendChild(arrow);

        route.appendChild(destination);


        /* -------------------------------------------------
           META
        ------------------------------------------------- */

        const meta =
            document.createElement("div");

        meta.className =
            "bus-meta";


        addMeta(
            meta,
            "Departure",
            bus.departureTime || "-"
        );


        addMeta(
            meta,
            "Official Route",
            (
                bus.officialOrigin ||
                "-"
            ) +
            " → " +
            (
                bus.officialDestination ||
                "-"
            )
        );


        /* -------------------------------------------------
           CARD
        ------------------------------------------------- */

        card.appendChild(busTop);

        card.appendChild(route);

        card.appendChild(meta);


        /* -------------------------------------------------
           ROUTE STOPS
        ------------------------------------------------- */

        if (
            Array.isArray(bus.stops) &&
            bus.stops.length > 0
        ) {

            const routeDetails =
                document.createElement("div");

            routeDetails.className =
                "route-details";


            const heading =
                document.createElement("h4");

            heading.textContent =
                "Route Stops";


            const timeline =
                document.createElement("div");

            timeline.className =
                "route-timeline";


            bus.stops.forEach(
                function (stop) {

                    const stopElement =
                        document.createElement("div");

                    stopElement.className =
                        "route-stop";


                    const stopName =
                        stop.stopName ||
                        "-";


                    const minutes =
                        stop.estimatedMinutesFromBoarding;


                    let timeText = "";


                    if (
                        minutes !== null &&
                        minutes !== undefined
                    ) {

                        if (
                            Number(minutes) === 0
                        ) {

                            timeText =
                                "Boarding point";

                        } else {

                            timeText =
                                "Approximately " +
                                minutes +
                                " min from boarding";

                        }

                    }


                    stopElement.innerHTML = `

                        <strong>
                            ${escapeHtml(stopName)}
                        </strong>

                        ${
                            timeText
                                ? `<small>${escapeHtml(timeText)}</small>`
                                : ""
                        }

                    `;


                    timeline.appendChild(
                        stopElement
                    );

                }
            );


            routeDetails.appendChild(
                heading
            );

            routeDetails.appendChild(
                timeline
            );

            card.appendChild(
                routeDetails
            );

        }


        /* -------------------------------------------------
           TIMINGS
        ------------------------------------------------- */

        if (
            Array.isArray(bus.timings) &&
            bus.timings.length > 0
        ) {

            const timingDetails =
                document.createElement("div");

            timingDetails.className =
                "route-details";


            const heading =
                document.createElement("h4");

            heading.textContent =
                "Estimated Timings";


            const timeline =
                document.createElement("div");

            timeline.className =
                "route-timeline";


            bus.timings.forEach(
                function (timing) {

                    const element =
                        document.createElement("div");

                    element.className =
                        "route-stop";


                    element.innerHTML = `

                        <strong>
                            ${escapeHtml(
                                timing.stopName || "-"
                            )}
                        </strong>

                        <small>
                            ${escapeHtml(
                                timing.estimatedArrivalTime ||
                                "-"
                            )}
                        </small>

                    `;


                    timeline.appendChild(
                        element
                    );

                }
            );


            timingDetails.appendChild(
                heading
            );

            timingDetails.appendChild(
                timeline
            );

            card.appendChild(
                timingDetails
            );

        }


        return card;

    }


    /* =====================================================
       ADD META ITEM
    ===================================================== */

    function addMeta(
        parent,
        label,
        value
    ) {

        const item =
            document.createElement("div");

        item.className =
            "meta-item";


        item.innerHTML = `

            <span>
                ${escapeHtml(label)}
            </span>

            <strong>
                ${escapeHtml(value)}
            </strong>

        `;


        parent.appendChild(item);

    }


    /* =====================================================
       HTML ESCAPE
    ===================================================== */

    function escapeHtml(
        value
    ) {

        if (
            value === null ||
            value === undefined
        ) {

            return "";

        }


        return String(value)

            .replace(
                /&/g,
                "&amp;"
            )

            .replace(
                /</g,
                "&lt;"
            )

            .replace(
                />/g,
                "&gt;"
            )

            .replace(
                /"/g,
                "&quot;"
            )

            .replace(
                /'/g,
                "&#039;"
            );

    }


    /* =====================================================
       HERO SLIDESHOW
    ===================================================== */

    const heroImages =
        document.querySelectorAll(
            ".hero-image"
        );


    let currentHero = 0;


    function changeHeroImage() {

        if (
            heroImages.length === 0
        ) {

            return;

        }


        heroImages[currentHero]
            .classList.remove(
                "active"
            );


        currentHero =
            (currentHero + 1) %
            heroImages.length;


        heroImages[currentHero]
            .classList.add(
                "active"
            );

    }


    /* Change every 2.5 seconds */

    setInterval(
        changeHeroImage,
        2500
    );

});