var array = ["c","a","l"];
var lambdas = [];

for (var index in array){
    lambdas.push(function(){return array[index];});
}

console.log(lambdas[0]());
console.log(lambdas[1]());
console.log(lambdas[2]());

