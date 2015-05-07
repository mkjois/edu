array = ["c","a","l"]
lambdas = []

for index in array:
    lambdas.append(lambda: array[index])

print(lambdas[0]())
print(lambdas[1]())
print(lambdas[2]())
