// src/main/resources/static/js/canvas.js
document.addEventListener('DOMContentLoaded', function() {
    var ctx = document.getElementById('dashboardChart');
    if (ctx) {
        new Chart(ctx, {
            type: 'bar',
            data: {
                labels: ['January', 'February', 'March', 'April', 'May', 'June'],
                datasets: [{
                    label: '# of Items Added',
                    data: [12, 19, 3, 5, 2, 3],
                    backgroundColor: [
                        '#A0724F', // --color-secondary
                        '#8B6349', // --color-tertiary
                        '#D4AF37', // --color-accent-gold
                        '#5C4033', // --color-primary
                        'rgba(43, 29, 20, 0.7)',  // A semi-transparent --color-dark
                        'rgba(160, 114, 79, 0.7)' // A semi-transparent --color-secondary
                    ],
                    borderColor: [
                        '#A0724F',
                        '#8B6349',
                        '#D4AF37',
                        '#5C4033',
                        '#2B1D14', // --color-dark
                        '#A0724F'  // --color-secondary
                    ],
                    borderWidth: 1
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                scales: {
                    y: {
                        beginAtZero: true
                    }
                },
                plugins: {
                    legend: {
                        display: true,
                        position: 'top',
                        labels: {
                            color: '#2B1D14', // Match var(--color-dark)
                            font: {
                                family: 'Poppins', // Match var(--font-body)
                            }
                        }
                    },
                    title: {
                        display: true,
                        text: 'Monthly Activity Overview',
                        color: '#5C4033', // Match var(--color-primary)
                        font: {
                            family: 'Playfair Display', // Match var(--font-heading)
                            size: 16,
                            weight: 'bold'
                        }
                    }
                }
            }
        });
    }
});
