(function () {
    const packages = [
        ["com.tlcsdm.jvmexplorer", "namespacecom_1_1tlcsdm_1_1jvmexplorer.html", "Desktop application entry points and top-level controllers."],
        ["com.tlcsdm.jvmexplorer.agent", "namespacecom_1_1tlcsdm_1_1jvmexplorer_1_1agent.html", "Runtime agent code injected into target JVM processes and client-side attach helpers."],
        ["com.tlcsdm.jvmexplorer.agent.launch", "namespacecom_1_1tlcsdm_1_1jvmexplorer_1_1agent_1_1launch.html", "Launch-time agent helpers used before the explorer attaches."],
        ["com.tlcsdm.jvmexplorer.bytecode", "namespacecom_1_1tlcsdm_1_1jvmexplorer_1_1bytecode.html", "Bytecode decompilation, disassembly and assembly helpers."],
        ["com.tlcsdm.jvmexplorer.bytecode.compile", "namespacecom_1_1tlcsdm_1_1jvmexplorer_1_1bytecode_1_1compile.html", "In-memory Java compilation support."],
        ["com.tlcsdm.jvmexplorer.fx", "namespacecom_1_1tlcsdm_1_1jvmexplorer_1_1fx.html", "Shared JavaFX UI utilities and controls."],
        ["com.tlcsdm.jvmexplorer.fx.classes", "namespacecom_1_1tlcsdm_1_1jvmexplorer_1_1fx_1_1classes.html", "Class browser views and class-tree UI logic."],
        ["com.tlcsdm.jvmexplorer.fx.compile", "namespacecom_1_1tlcsdm_1_1jvmexplorer_1_1fx_1_1compile.html", "UI for compiling and applying Java source changes."],
        ["com.tlcsdm.jvmexplorer.fx.jvms", "namespacecom_1_1tlcsdm_1_1jvmexplorer_1_1fx_1_1jvms.html", "JVM discovery and selection UI."],
        ["com.tlcsdm.jvmexplorer.fx.method", "namespacecom_1_1tlcsdm_1_1jvmexplorer_1_1fx_1_1method.html", "Method modification UI."],
        ["com.tlcsdm.jvmexplorer.fx.openclass", "namespacecom_1_1tlcsdm_1_1jvmexplorer_1_1fx_1_1openclass.html", "Open-class dialogs and related UI."],
        ["com.tlcsdm.jvmexplorer.helper", "namespacecom_1_1tlcsdm_1_1jvmexplorer_1_1helper.html", "Application helper utilities."],
        ["com.tlcsdm.jvmexplorer.net", "namespacecom_1_1tlcsdm_1_1jvmexplorer_1_1net.html", "Client-side connection and packet response handling."],
        ["com.tlcsdm.jvmexplorer.preferences", "namespacecom_1_1tlcsdm_1_1jvmexplorer_1_1preferences.html", "Persistent application preference handling."],
        ["com.tlcsdm.jvmexplorer.protocol", "namespacecom_1_1tlcsdm_1_1jvmexplorer_1_1protocol.html", "Shared protocol messages, descriptors and configuration models."],
        ["com.tlcsdm.jvmexplorer.protocol.helper", "namespacecom_1_1tlcsdm_1_1jvmexplorer_1_1protocol_1_1helper.html", "Shared protocol helper utilities."],
        ["com.tlcsdm.jvmexplorer.settings", "namespacecom_1_1tlcsdm_1_1jvmexplorer_1_1settings.html", "Application settings and configuration UI support."]
    ];

    function addSidebarToggle() {
        const sideNav = document.getElementById("side-nav");
        const docContent = document.getElementById("doc-content");
        if (!sideNav || !docContent || document.getElementById("sidebar-toggle")) {
            return;
        }
        const button = document.createElement("button");
        button.id = "sidebar-toggle";
        button.type = "button";
        const setState = (collapsed) => {
            document.body.classList.toggle("sidebar-collapsed", collapsed);
            button.textContent = collapsed ? "Show navigation" : "Hide navigation";
            button.setAttribute("aria-expanded", String(!collapsed));
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
    }

    function packageCards() {
        const grid = document.createElement("div");
        grid.className = "package-grid";
        packages.forEach(([name, href, description]) => {
            const card = document.createElement("div");
            card.className = "package-card";
            const link = document.createElement("a");
            link.href = href;
            link.textContent = name;
            const text = document.createElement("p");
            text.textContent = description;
            card.append(link, text);
            grid.appendChild(card);
        });
        return grid;
    }

    function enhancePackageList() {
        if (!/namespaces\.html$/.test(window.location.pathname)) {
            return;
        }
        const contents = document.querySelector(".contents");
        if (!contents) {
            return;
        }
        contents.innerHTML = "";
        const intro = document.createElement("div");
        intro.className = "textblock";
        intro.textContent = "Java packages are listed directly below. Use these links instead of expanding the generic top-level com namespace.";
        contents.append(intro, packageCards());
    }

    document.addEventListener("DOMContentLoaded", () => {
        addSidebarToggle();
        enhancePackageList();
    });
}());
