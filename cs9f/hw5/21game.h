#ifndef GAME_H
#define GAME_H
#include <vector>
#include <string>
#include "cards.h"
class Player {
public:
	Player (string name);
	// Reinitialize the hand to contain no cards.
	virtual void Reset ();
	// Add the given card to the hand.
	void AddCard (Card c);
	// Return true if the player wants another card.
	virtual bool IsStillDrawing (Card c) = 0;
	// Return the player's total hand value.
	int Total ();
	// Tally a win or a loss.
	void Tally (int dealerTotal);
	// Tally a loss.
	void TallyLoss ();
	// Report how many wins.
	void Report ();
protected:
	string myName;
	vector<Card> myHand;
	int myCardCount;
	int myAcesAs11Count;
	int myTotal;
	int myWinCount;
	int myGameCount;
};
class Group21 {
public:
	Group21 ();
	void PlayGame (Deck &);
	void Report ();
private:
	vector<Player*> allPlayers;
};
#endif
