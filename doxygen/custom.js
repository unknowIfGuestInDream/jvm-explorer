(function () {
    function addSidebarToggle() {
        const sideNav = document.getElementById("side-nav");
        const docContent = document.getElementById("doc-content");
        if (!sideNav || !docContent || document.getElementById("sidebar-toggle")) {
            return;
        }
        const button = document.createElement("button");
        button.id = "sidebar-toggle";
        button.type = "button";
        const updatePosition = () => {
            if (document.body.classList.contains("sidebar-collapsed")) {
                button.style.left = "18px";
                return;
            }
            const sideNavWidth = sideNav.getBoundingClientRect().width;
            const buttonWidth = button.getBoundingClientRect().width || 140;
            const gap = 14;
            const minimumLeft = 18;
            if (sideNavWidth < buttonWidth + (gap * 2)) {
                button.style.left = `${minimumLeft}px`;
                return;
            }
            button.style.left = `${Math.max(minimumLeft, sideNavWidth - buttonWidth - gap)}px`;
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
        if (typeof ResizeObserver !== "undefined") {
            new ResizeObserver(updatePosition).observe(sideNav);
        }
    }

    document.addEventListener("DOMContentLoaded", () => {
        addSidebarToggle();
    });
}());
