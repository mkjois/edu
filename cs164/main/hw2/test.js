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

var array = ["c","a","l"];
var lambdas = [];

var iterator = makeIterator(array);
var _u1 = iterator.next();
while (_u1 !== null) {
    var index = _u1;
    lambdas.push(function() { return array[index]; });
    _u1 = iterator.next();
}

console.log(lambdas[0]());
console.log(lambdas[1]());
console.log(lambdas[2]());
