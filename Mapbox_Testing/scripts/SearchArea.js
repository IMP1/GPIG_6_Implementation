// SearchArea Class
var searchAreaID = 0;

function SearchArea(){
    this.id = searchAreaID++;
    this.center = [];
    this.outer  = [];
    this.assignedDrones = 1;
    this.complete = false;
}