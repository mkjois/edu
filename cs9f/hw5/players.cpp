#include <string>
using namespace std;
#include "21game.h"
#include "players.h"
#include "cards.h"

Dealer::Dealer(string name) : Player(name) {};
Smarter::Smarter(string name) :Player(name) {};
OneMoreTaker::OneMoreTaker(string name) :Player(name) {
    oneMore = true;
}

bool Dealer::IsStillDrawing(Card c) {
    return myTotal < 17;
}

Card Dealer::Upcard() {
    myUpCard = myHand[1];
    return myUpCard;
}

bool Smarter::IsStillDrawing(Card c) {
    if (c.Value() >= 7) {
        return myTotal < 17;
    } else {
        return myTotal < 12;
    }
}

bool OneMoreTaker::IsStillDrawing(Card c) {
    if (myTotal < 17) {
        oneMore = true;
        return true;
    } else if (oneMore) {
        oneMore = false;
        return true;
    } else {
        return false;
    }
}

void OneMoreTaker::Reset() {
    myCardCount = myAcesAs11Count = 0;
	myTotal = 0;
    oneMore = true; // OneMoreTaker is allowed to take one more
}
