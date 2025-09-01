document.addEventListener('DOMContentLoaded', function () {
    const sidebar = document.getElementById('sidebar');
    const content = document.getElementById('content');
    const sidebarCollapse = document.getElementById('sidebarCollapse');

    if (sidebar && content && sidebarCollapse) {
        sidebarCollapse.addEventListener('click', function () {
            sidebar.classList.toggle('active');
            content.classList.toggle('active');
        });

        // Close sidebar if clicking outside on small screens when it's open
        content.addEventListener('click', function (event) {
            if (window.innerWidth <= 768 && sidebar.classList.contains('active')) {
                // Check if the click was not on the sidebar itself
                if (!sidebar.contains(event.target) && event.target !== sidebarCollapse && !sidebarCollapse.contains(event.target)) {
                    sidebar.classList.remove('active');
                    content.classList.remove('active');
                }
            }
        });
    }

    // Set active class for sidebar navigation links based on current URL
    const navLinks = document.querySelectorAll('#sidebar ul li a');
    const currentPath = window.location.pathname;

    navLinks.forEach(link => {
        if (link.getAttribute('href') === currentPath) {
            link.parentElement.classList.add('active');
        }
    });
});

let dimensionIndex = 1; // start after first row

    document.getElementById('add-dimension-btn').addEventListener('click', function() {
        // Get the first dimension row as template
        const templateRow = document.querySelector('.dimension-row');
        const clone = templateRow.cloneNode(true);

        // Update IDs and th:fields for the new row
        clone.querySelectorAll('input').forEach(input => {
            // Update name attribute
            const oldName = input.getAttribute('name');
            if (oldName) {
                input.setAttribute('name', oldName.replace(/\[\d+\]/, `[${dimensionIndex}]`));
            }

            // Optional: update th:field if needed
            const oldField = input.getAttribute('th:field');
            if (oldField) {
                input.setAttribute('th:field', oldField.replace(/\[\d+\]/, `[${dimensionIndex}]`));
            }

            // Update ID
            const oldId = input.id;
            input.id = oldId.replace(/\d+$/, dimensionIndex);

            // Clear value
            input.value = '';
        });
        // Append the new row
        templateRow.parentNode.insertBefore(clone, this); // insert before the button

        dimensionIndex++;
    });
