(function () {
    function makeFileListNamesClickable() {
        document.querySelectorAll("table.directory td.entry").forEach((entryCell) => {
            // Doxygen renders file rows as an icon link followed by a bold file name.
            // Reuse the generated source-page href so the visible file name is clickable too.
            const iconLink = entryCell.querySelector(":scope > a[href] > span.icondoc");
            const fileNameElement = entryCell.querySelector(":scope > b");
            if (!iconLink || !fileNameElement) {
                return;
            }
            const href = iconLink.parentElement.getAttribute("href");
            if (!href) {
                return;
            }
            const linkedName = document.createElement("a");
            linkedName.className = "el";
            linkedName.href = href;
            linkedName.textContent = fileNameElement.textContent;
            fileNameElement.replaceChildren(linkedName);
        });
    }

    function addSidebarToggle() {
        const sideNav = document.getElementById("side-nav");
        const docContent = document.getElementById("doc-content");
        if (!sideNav || !docContent || document.getElementById("sidebar-toggle")) {
            return;
        }
        const button = document.createElement("button");
        button.id = "sidebar-toggle";
        button.type = "button";
        const DEFAULT_BUTTON_WIDTH = 140;
        const BUTTON_GAP = 14;
        const MINIMUM_LEFT_POSITION = 18;
        const updatePosition = () => {
            if (document.body.classList.contains("sidebar-collapsed")) {
                button.style.left = `${MINIMUM_LEFT_POSITION}px`;
                return;
            }
            const sideNavWidth = sideNav.getBoundingClientRect().width;
            const buttonWidth = button.getBoundingClientRect().width || DEFAULT_BUTTON_WIDTH;
            if (sideNavWidth < buttonWidth + (BUTTON_GAP * 2)) {
                button.style.left = `${MINIMUM_LEFT_POSITION}px`;
                return;
            }
            button.style.left = `${Math.max(MINIMUM_LEFT_POSITION, sideNavWidth - buttonWidth - BUTTON_GAP)}px`;
        };
        const setState = (collapsed) => {
            document.body.classList.toggle("sidebar-collapsed", collapsed);
            button.textContent = collapsed ? "Show navigation" : "Hide navigation";
            button.setAttribute("aria-expanded", String(!collapsed));
            window.requestAnimationFrame(updatePosition);
            try {
                window.localStorage.setItem("doxygen-sidebar-collapsed", collapsed ? "true" : "false");
            }
            catch (e) {
                // Ignore storage restrictions in local file previews.
            }
        };
        button.addEventListener("click", () => setState(!document.body.classList.contains("sidebar-collapsed")));
        document.body.appendChild(button);
        let collapsed = false;
        try {
            collapsed = window.localStorage.getItem("doxygen-sidebar-collapsed") === "true";
        }
        catch (e) {
            collapsed = false;
        }
        setState(collapsed);
        window.addEventListener("resize", updatePosition);
        if (window.jvmExplorerSidebarToggleObserver) {
            window.jvmExplorerSidebarToggleObserver.disconnect();
        }
        if (typeof ResizeObserver !== "undefined") {
            window.jvmExplorerSidebarToggleObserver = new ResizeObserver(updatePosition);
            window.jvmExplorerSidebarToggleObserver.observe(sideNav);
        }
    }

    document.addEventListener("DOMContentLoaded", () => {
        makeFileListNamesClickable();
        addSidebarToggle();
    });
}());
