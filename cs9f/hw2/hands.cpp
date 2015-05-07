#include <iostream>
#include <string>
#include "cards.h"
#include "hands.h"

using namespace std;

DealerHand::DealerHand () {
    myUpCard;
    myCardCount = 0;
    myAceAs11Count = 0;
    myTotal = 0;
}

void DealerHand::ReduceAceValues () {
    while (myTotal > 21 && myAceAs11Count > 0) {
        myTotal -= 10;
        myAceAs11Count--;
        cout << "\nAn Ace had to be reduced from 11 to 1." << endl;
    }
}

void DealerHand::Reset () {
    myCardCount = 0;
    myAceAs11Count = 0;
    myTotal = 0;
}

void DealerHand::AddCard (Card c) {
    if (myCardCount == 0) {
        myUpCard = c;
    }
    myCardCount++;
    if (c.IsAce()) {
        myAceAs11Count++;
    }
    myTotal += c.Value();
    DealerHand::ReduceAceValues();
}

bool DealerHand::CanDraw () {
    if (DealerHand::Total() < 17 ) {
        return true;
    } else {
        return false;
    }
}

Card DealerHand::UpCard () {
    return myUpCard;
}

int DealerHand::Total () {
    return myTotal;
}

void DealerHand::Print () {
    cout << endl;
    cout << "Dealer up-card:\t" << DealerHand::UpCard().Name() << endl;
    cout << "Dealer cards:\t" << myCardCount << endl;
    cout << "Dealer total:\t" << DealerHand::Total() << endl;
    cout << "Dealer aces as 11:\t" << myAceAs11Count << endl;
}

CustomerHand::CustomerHand () {
    myCardCount = 0;
    myAceAs11Count = 0;
    myTotal = 0;
}

void CustomerHand::ReduceAceValues () {
    while (myTotal > 21 && myAceAs11Count > 0) {
        myTotal -= 10;
        myAceAs11Count--;
        cout << "\nAn Ace had to be reduced from 11 to 1." << endl;
    }
}

void CustomerHand::Reset () {
    myCardCount = 0;
    myAceAs11Count = 0;
    myTotal = 0;
}

void CustomerHand::AddCard (Card c) {
    myCardCount++;
    if (c.IsAce()) {
        myAceAs11Count++;
    }
    myTotal += c.Value();
    CustomerHand::ReduceAceValues();
}

bool CustomerHand::CanDraw (Card dealerUpCard) {
    if (CustomerHand::Total() < 12) {
        return true;
    } else if (CustomerHand::Total() < 17 && dealerUpCard.Value() >= 7) {
        return true;
    } else {
        return false;
    }
}

int CustomerHand::Total () {
    return myTotal;
}

void CustomerHand::Print () {
    cout << endl;
    cout << "Customer cards:\t" << myCardCount << endl;
    cout << "Customer total:\t" << CustomerHand::Total() << endl;
    cout << "Customer aces as 11:\t" << myAceAs11Count << endl;
}
