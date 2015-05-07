#ifndef PLAYERS_H
#define PLAYERS_H
#include <string>
using namespace std;
#include "cards.h"
#include "21game.h"


class Dealer : public Player {
public:
    Dealer(string name);
    bool IsStillDrawing(Card c);
    Card Upcard();
private:
    Card myUpCard;
};

class Smarter : public Player {
public:
    Smarter(string name);
    bool IsStillDrawing(Card c);
};

class OneMoreTaker : public Player {
public:
    OneMoreTaker(string name);
    bool IsStillDrawing(Card c);
    void Reset();
private:
    bool oneMore;
};

#endif
