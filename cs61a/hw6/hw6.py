"""61A Homework 6
Name: Manohar Jois
Login: cs61a-3u
TA: Allen Nguyen
Section: 203
"""

from doctest import run_docstring_examples
test = run_docstring_examples

# Q1.

class VendingMachine(object):
    """A vending machine that vends some product for some price.

    >>> v = VendingMachine('crab', 10)
    >>> v.vend()
    'Machine is out of stock.'
    >>> v.restock(2)
    'Current crab stock: 2'
    >>> v.vend()
    'You must deposit $10 more.'
    >>> v.deposit(7)
    'Current balance: $7'
    >>> v.vend()
    'You must deposit $3 more.'
    >>> v.deposit(5)
    'Current balance: $12'
    >>> v.vend()
    'Here is your crab and $2 change.'
    >>> v.deposit(10)
    'Current balance: $10'
    >>> v.vend()
    'Here is your crab.'
    >>> v.deposit(15)
    'Machine is out of stock. Here is your $15.'
    """
    no_more = "Machine is out of stock."

    def __init__(self, item, price):
        self.item = item
        self.price = price
        self.stock = 0
        self.funds = 0
        self.confirm = "Here is your " + item

    def restock(self, units):
        self.stock += units
        return "Current " + self.item + " stock: " + str(self.stock)

    def deposit(self, amount):
        if self.stock == 0:
            return self.no_more + " Here is your $" + str(amount) + "."
        self.funds += amount
        return "Current balance: $" + str(self.funds)

    def vend(self):
        if self.stock == 0: return self.no_more
        diff = self.price - self.funds
        if diff > 0: return "You must deposit $" + str(diff) + " more."
        else:
            self.stock -= 1
            self.funds = 0
            if diff == 0: change = "."
            else: change = " and $" + str(-diff) + " change."
            return self.confirm + change

# Q2.

class MissManners(object):
    """A container class that only forward messages that say please.

    >>> v = VendingMachine('teaspoon', 10)
    >>> v.restock(2)
    'Current teaspoon stock: 2'
    >>> m = MissManners(v)
    >>> m.ask('vend')
    'You must learn to say please.'
    >>> m.ask('please vend')
    'You must deposit $10 more.'
    >>> m.ask('please deposit', 20)
    'Current balance: $20'
    >>> m.ask('now will you vend?')
    'You must learn to say please.'
    >>> m.ask('please give up a teaspoon')
    'Thanks for asking, but I know not how to give up a teaspoon'
    >>> m.ask('please vend')
    'Here is your teaspoon and $10 change.'
    """
    manners = "You must learn to say please."
    unknown = "Thanks for asking, but I know not how to"

    def __init__(self, obj):
        self.obj = obj

    def ask(self, clause, *args):
        words = clause.split()
        if words[0].lower() == "please":
            words.pop(0)
            response = self.unknown
            for word in words:
                if hasattr(self.obj, word):
                    return getattr(self.obj, word)(*args)
                response = response + " " + word
            return response
        else: return self.manners

# Q3.

class Amount(object):
    """An amount of nickels and pennies.

    >>> a = Amount(3, 7)
    >>> a.nickels
    3
    >>> a.pennies
    7
    >>> a.value
    22
    >>> a.nickels = 5
    >>> a.value
    32
    """
    def __init__(self, n, p):
        self.nickels = n
        self.pennies = p

    @property
    def value(self):
        return 5*self.nickels + self.pennies

class MinimalAmount(Amount):
    """An amount of nickels and pennies that is initialized with no more than
    four pennies, by converting excess pennies to nickels.

    >>> a = MinimalAmount(3, 7)
    >>> a.nickels
    4
    >>> a.pennies
    2
    >>> a.value
    22
    """
    def __init__(self, n, p):
        self.nickels = n + p//5
        self.pennies = p%5

# Q4.

class Container(object):
    """A container for a single item.

    >>> c = Container(12)
    >>> c
    Container(12)
    >>> len(c)
    1
    >>> c[0]
    12
    """

    def __init__(self, item):
        self._item = item

    def __repr__(self):
        return 'Container({0})'.format(repr(self._item))

    def __len__(self):
        return 1

    def __getitem__(self, index):
        assert index == 0, 'A container holds only one item'
        return self._item

class Rlist(object):
    """A recursive list consisting of a first element and the rest.

    >>> s = Rlist(1, Rlist(2, Rlist(3)))
    >>> len(s)
    3
    >>> s[0]
    1
    >>> s[1]
    2
    >>> s[2]
    3
    """
    def __init__(self, first, rest=None):
        self._first = first
        self._rest = rest

    def __len__(self):
        count, copy = 1, self
        while copy._rest is not None:
            count += 1
            copy = copy._rest
        return count

    def __getitem__(self, index):
        copy = self
        while index >= 0:
            item = copy._first
            copy = copy._rest
            index -= 1
        return item

    def __repr__(self):
        return "Rlist({0}, {1})".format(self._first, self._rest)

# Q5.

def make_instance(cls):
    """Return a new object instance."""
    def get_value(name):
        if name in attributes:
            return attributes[name]
        else:
            value = cls['get'](name)
            return bind_method(value, instance)

    def set_value(name, value):
        attributes[name] = value

    attributes = {}
    instance = {'get': get_value, 'set': set_value}
    return instance

def bind_method(value, instance):
    """Return value or a bound method if value is callable."""
    if callable(value):
        def method(*args):
            return value(instance, *args)
        return method
    else:
        return value

def make_class(attributes, base_classes=()):
    """Return a new class.

    attributes -- class attributes
    base_classes -- a sequence of classes
    """
    "*** YOUR CODE HERE ***"

def init_instance(cls, *args):
    """Return a new instance of cls, initialized with args."""
    instance = make_instance(cls)
    init = cls['get']('__init__')
    if init:
        init(instance, *args)
    return instance

def make_account_class():
    """Return the Account class, which has deposit and withdraw methods."""

    interest = 0.02

    def __init__(self, account_holder):
        self['set']('holder', account_holder)
        self['set']('balance', 0)

    def deposit(self, amount):
        """Increase the account balance by amount and return the new balance."""
        new_balance = self['get']('balance') + amount
        self['set']('balance', new_balance)
        return self['get']('balance')

    def withdraw(self, amount):
        """Decrease the account balance by amount and return the new balance."""
        balance = self['get']('balance')
        if amount > balance:
            return 'Insufficient funds'
        self['set']('balance', balance - amount)
        return self['get']('balance')

    return make_class(locals())

Account = make_account_class()

def make_fee_class():
    """Return a Fee class, which has a fee class attribute."""
    fee = 1
    return make_class(locals())

Fee = make_fee_class()

def make_checking_account_class():
    """Return the CheckingAccount class, which imposes a $1 withdrawal fee.

    >>> jack_acct = CheckingAccount['new']('Jack')
    >>> jack_acct['get']('interest')
    0.01
    >>> jack_acct['get']('deposit')(20)
    20
    >>> jack_acct['get']('withdraw')(5)
    14
    """
    interest = 0.01

    def withdraw(self, amount):
        fee = self['get']('fee')
        return Account['get']('withdraw')(self, amount + fee)

    return make_class(locals(), [Account, Fee])

CheckingAccount = make_checking_account_class()
