const ctx = document.getElementById('dashboardChart');
let sidebar=document.querySelector(".side-bar");
let body=document.body;

console.log(ctx);
console.log("ashraf");
fetch('/dashboard-data')
    .then(res => res.json())
    .then(data => {
        const ctx = document.getElementById('dashboardChart');
        new Chart(ctx, {
            type: 'bar',
            data: {
                labels: data.labels,
                datasets: [
                    {
                        label: 'Messages',
                        data: data.messages,
                        backgroundColor: 'rgba(255, 99, 132, 0.6)'
                    }
                ]
            },
            options: {
                responsive: true,
                plugins: {
                    legend: { position: 'top' }
                }
            }
        });
    });
let ic=document.querySelector("header .userInfo");

ic.onclick=function(){
 document.querySelector(".header .logout").classList.toggle("open");
}
document.querySelector(".menu-btn").onclick = () =>{
   sidebar.classList.toggle("active");
       body.classList.remove("active");

}
document.querySelector(".side-bar .closebtn").onclick = ()=>{
    sidebar.classList.remove("active");
    body.classList.remove("active");

}
