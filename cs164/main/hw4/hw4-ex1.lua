function fibCo(a, b)
    coroutine.yield(a)
    fibCo(b, a + b)
end

function fib()
    return coroutine.wrap(function() fibCo(0,1) end)
end

function takeCo(stream, n)
    while n > 0 do
        coroutine.yield(stream())
        n = n-1
    end
end

function take(stream, n)
    return coroutine.wrap(function() takeCo(stream, n) end)
end

function filterCo(stream, prop)
    while true do
        local val = stream()
        while not prop(val) do
            val = stream()
        end
        coroutine.yield(val)
    end
end

function filter(stream, prop)
    return coroutine.wrap(function() filterCo(stream, prop) end)
end

function mapCo(stream, f)
    while true do
        coroutine.yield(f(stream()))
    end
end

function map(stream, f)
    return coroutine.wrap(function() mapCo(stream, f) end)
end

isEven = function(x) return x % 2 == 0 end
plusOne = function(x) return x + 1 end

fibStream = fib()
evenFib = filter(fibStream, isEven)
evenFibPlusOne = map(evenFib, plusOne)
for i in take(evenFibPlusOne, 5) do
    print(i)
end
