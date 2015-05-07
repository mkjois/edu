#ifndef _HANDS
#define _HANDS
#include <iostream>
#include "cards.h"

using namespace std;

class DealerHand {
public:
	// Construct an empty hand for the dealer.
	DealerHand ();
	// Reinitialize the hand to contain no cards.
	void Reset ();
	// Add the given card to the hand.
	void AddCard (Card c);
	// Return true if, according to the dealer's strategy, another
	// card should be drawn.  (The dealer's strategy is to draw a
	// card when his/her total is less than or equal to 16.)
	bool CanDraw ();
	// Return the dealer's "up card" (the first card dealt to the
	// dealer at the start of each game).
	Card UpCard ();
	// Return the dealer's total hand value.
	int Total ();
	// Print available information about the hand.
	void Print ();
private:
	Card myUpCard;
	int myCardCount;
	int myAceAs11Count;
	int myTotal;
    void ReduceAceValues ();
};
class CustomerHand {
public:
	// Construct an empty hand for the customer.
	CustomerHand ();
	// Reinitialize the hand to contain no cards.
	void Reset ();
	// Add the given card to the hand.
	void AddCard (Card c);
	// Return true if, according to the player's strategy, another
	// card should be drawn.
	bool CanDraw (Card dealerUpCard);
	// Return the total hand value.
	int Total ();
	// Print available information about the hand.
	void Print ();
private:
	int myCardCount;
	int myAceAs11Count;
	int myTotal;
    void ReduceAceValues ();
};
#endif
