// SearchArea Class
var searchAreaID = 0;

function SearchArea(){
    this.id = searchAreaID++;
    this.center = [];
    this.outer  = [];
    this.radius = 0;
    this.drawRadius = 0;
    this.assignedDrones = 1;
    this.complete = false;
}

function deleteSearchArea(searchArea){        
    deleteSearchAreaView(searchArea);    
    removeByAttr(searchAreaArray, 'id', searchArea.id);
}