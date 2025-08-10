
document.querySelector(".gallery").addEventListener("click",(e)=>{
if(e.target && e.target.classList.contains("imgInAG")){
const typeId=e.target.getAttribute("data-gallery-id");
console.log(typeId);
window.location.href=`/admingalleryType/${typeId}`;

}
})
