function makeIterator(array){
    var nextIndex = 0;
    
    return {
       next: function(){
           return nextIndex < array.length ?
               nextIndex++ :
               null;
       }
    }
}
