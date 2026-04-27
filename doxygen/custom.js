(function () {
    function textFromTitle() {
        var title = document.querySelector('.headertitle .title');
        return title ? title.textContent.replace(/\s+/g, ' ').trim() : document.title;
    }

    function enhanceNavPath() {
        var navPath = document.getElementById('nav-path');
        var docContent = document.getElementById('doc-content');
        if (!navPath || !docContent || navPath.classList.contains('navpath-top')) {
            return;
        }

        var list = navPath.querySelector('ul');
        if (list && !list.querySelector('li')) {
            var home = document.createElement('li');
            var homeLink = document.createElement('a');
            homeLink.href = 'index.html';
            homeLink.textContent = 'Main Page';
            home.appendChild(homeLink);

            var current = document.createElement('li');
            current.textContent = textFromTitle();

            list.appendChild(home);
            list.appendChild(current);
        }

        navPath.classList.add('navpath-top');
        docContent.parentNode.insertBefore(navPath, docContent);
    }

    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', enhanceNavPath);
    } else {
        enhanceNavPath();
    }
}());
