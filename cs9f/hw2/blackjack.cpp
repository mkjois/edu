#include <iostream>
#include <string>
#include "cards.h"
#include "hands.h"

using namespace std;

// You complete this function, which takes two hands and a deck as arguments.
// It returns the result of dealing two cards each to the dealer hand and
// the customer hand arguments.  The first card in the deck argument goes
// to the customer, the second to the dealer, the third to the customer,
// and the fourth to the dealer.
// DealFirstFourCards should also print appropriate debugging output.
void DealFirstFourCards (DealerHand & dealerH, CustomerHand & custH, Deck d) {
    d.Shuffle();
    dealerH.Reset();
    custH.Reset();
    cout << "\nThe hands have been reset." << endl
         << "The deck has been shuffled." << endl
         << "It's game time.\n" <<  endl;
	Card card;
	for (int i = 0; i < 4; i++) {
	    card = d.Deal();
        if (i % 2 == 0) {
            custH.AddCard(card);
            cout << "Customer dealt:\t" << card.Name() << endl;
        } else {
            dealerH.AddCard(card);
            cout << "Dealer dealt:\t" << card.Name() << endl;
        }
    }
    custH.Print();
    dealerH.Print();
}

// Checks totals after completion of dealing phase,
// assuming neither player bust.
// Returns 1 for a customer win and 0 for a dealer win.
int WinLoss (DealerHand dealerH, CustomerHand custH) {
    if (custH.Total() <= dealerH.Total()) { // Dealer win
        cout << "LOSS: Dealer wins " << dealerH.Total()
             << " to " << custH.Total() << endl;
        return 0;
    } else { // Customer win
        cout << "WIN: Customer wins " << custH.Total()
             << " to " << dealerH.Total() << endl;
        return 1;
    }
}

// Draws cards for the dealer until they stand or bust.
// Returns whether or not they bust.
bool DealerBust (DealerHand & dealerH, Deck & deck) {
    Card c;
    while (dealerH.CanDraw()) {
        c = deck.Deal();
        dealerH.AddCard(c);
        cout << "Dealer dealt:\t" << c.Name() << endl;
        if (dealerH.Total() > 21) { // Dealer bust
            dealerH.Print();
            cout << endl << "WIN: Dealer busts with " << dealerH.Total()
                 << endl;
            return true;
        }
    }
    cout << endl << "Dealer stands." << endl;
    dealerH.Print();
    return false;
}

// Draws cards for the customer until they stand or bust.
// Returns whether or not they bust.
bool CustomerBust (CustomerHand & custH, DealerHand dealerH, Deck & deck) {
    Card c;
    while (custH.CanDraw(dealerH.UpCard())) {
        c = deck.Deal();
        custH.AddCard(c);
        cout << "Customer dealt:\t" << c.Name() << endl;
        if (custH.Total() > 21) { // Customer bust
            custH.Print();
            cout << endl << "LOSS: Customer busts with " << custH.Total()
                 << endl;
            return true;
        }
    }
    cout << endl << "Customer stands." << endl;
    custH.Print();
    return false;
}

// You complete this function, which takes two hands and a deck as arguments.
// On entry, each hand has already been dealt two cards, and neither hand has
// a blackjack (a total of 21). ResultOfPlay returns the result of playing the
// hand. First, the customer draws cards until he/she either goes past 21
// (a "bust") or reaches a suitable total less than or equal to 21. If the
// customer has not bust, then the dealer draws cards until reaching a total of
// 17 or more. ResultOfPlay returns 0 if the customer busts or if the customer's
// total is less than or equal to the dealer's and the dealer hasn't bust.
// ResultOfPlay returns 1 if the dealer busts or if the customer's total
// exceeds the dealer's and the customer has not bust.
// ResultOfPlay should also print appropriate debugging output.
int ResultOfPlay (DealerHand dealerH, CustomerHand custH, Deck deck) {
    if (CustomerBust(custH, dealerH, deck)) {
        return 0;
    } else if (DealerBust(dealerH, deck)) {
        return 1;
    } else {
        return WinLoss(dealerH, custH);
    }
}

int main () {
	Deck d(false);
	int numGames, numWins;
	DealerHand dealerHand;
	CustomerHand ourHand;
	cout << "How many games? ";
	cin >> numGames;
	numWins = 0;
	for (int k=1; k<=numGames; k++) {
		DealFirstFourCards (dealerHand, ourHand, d);
		if (dealerHand.Total()==21) {
			cout << "LOSS: Dealer has blackjack" << endl;
		} else if (ourHand.Total()==21) {
			numWins += 2;
			cout << "WIN:  We have blackjack" << endl;
		} else {
			numWins += ResultOfPlay (dealerHand, ourHand, d);
		}
	}
	cout << "We won " << numWins << " out of " << numGames << endl;
	return 0;
}
