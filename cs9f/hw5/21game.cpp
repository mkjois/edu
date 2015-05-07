#include <iostream>
#include <vector>
#include <string>
#include <cstdlib>
using namespace std;
#include "21game.h"
#include "players.h"
#include "cards.h"
const int NUMPLAYERS = 1;	// doesn't include the dealer
// Set up the group of Twenty-one players.
Group21::Group21 (): allPlayers(NUMPLAYERS+1) {
	string response1, response2;
	allPlayers[0] = new Dealer ("dealer");
	for (int k=1; k<=NUMPLAYERS; k++) {
		cout << "Player name? ";
		cin >> response1;
		cout << "Player strategy? ";
		cin >> response2;
		if (response2 == "dealer") {
			allPlayers[k] = new Dealer (response1);
		} else if (response2 == "smart") {
			allPlayers[k] = new Smarter (response1);
		} else if (response2 == "onemore") {
			allPlayers[k] = new OneMoreTaker (response1);
		} else {
			cout << "I don't know that strategy." << endl;
			exit (1);
		}
		cout << endl;
	}
}

// Play a single game for all players.
void Group21::PlayGame (Deck &d) {
	// Initialize all the hands.
	for (int k=0; k<=NUMPLAYERS; k++) {
		allPlayers[k]->Reset();
	}
	// Deal initial cards.
	for (int j=0; j<2; j++) {
		for (int k=1; k<=NUMPLAYERS; k++) {
			allPlayers[k]->AddCard(d.Deal());
		}
		allPlayers[0]->AddCard(d.Deal());
	}
	// Check for dealer blackjack.
	if (allPlayers[0]->Total() == 21) {
		for (int k=1; k<=NUMPLAYERS; k++) {
			allPlayers[k]->TallyLoss();
		}
	} else {
		// Why is the type cast needed?
		Card dealerUpCard = ((Dealer*) allPlayers[0])->Upcard();
		// Have all the players play.
		for (int k=1; k<=NUMPLAYERS; k++) {
			while (allPlayers[k]->IsStillDrawing(dealerUpCard)) {
				allPlayers[k]->AddCard(d.Deal());
			}
			cout << endl;
		}
		// Have the dealer play.
		while (allPlayers[0]->IsStillDrawing(dealerUpCard)) {
			allPlayers[0]->AddCard(d.Deal());
		}
		// Determine outcomes.
		for (int k=1; k<=NUMPLAYERS; k++) {
			allPlayers[k]->Tally(allPlayers[0]->Total());
		}
		cout << endl;
	}
}
// Have all the players report how many games they've won.
void Group21::Report () {
	for (int k=1; k<=NUMPLAYERS; k++) {
		allPlayers[k]->Report ();
	}
}
// Constructor (the argument is the player's name)
Player::Player (string s): myHand(10) {
	myName = s;
	myCardCount = myAcesAs11Count = 0;
	myTotal = 0;
	myWinCount = myGameCount = 0;
}
// Reset the player's hand to empty.
void Player::Reset () {
	myCardCount = myAcesAs11Count = 0;
	myTotal = 0;
}
// Add the given card to the player's hand.
void Player::AddCard (Card c) {
	myHand[myCardCount] = c;
	myCardCount++;
	if (c.IsAce ()) {
		if (myAcesAs11Count > 0) {
			myTotal += 1;
		} else {
			myAcesAs11Count++;
			myTotal += 11;
		}
	} else {
		myTotal += c.Value();
	}
	if (myTotal>21 && myAcesAs11Count>0) {
		myTotal -= 10;
		myAcesAs11Count--;
	}
}

// Return the player's hand total.
int Player::Total () {
	return myTotal;
}

// Compare the player's total to the dealer's total,
// and tally a win or loss appropriately.
// Precondition: the dealer doesn't have a blackjack.
void Player::Tally (int dealerTotal) {
	if (myTotal==21 && myCardCount==2) {
		myWinCount += 2;
		cout << myName << " wins with a blackjack!" << endl;
	} else if (myTotal <= 21) {
		if (myTotal>dealerTotal || dealerTotal>21) {
			myWinCount++;
			cout << myName << " wins." << endl;
		} else {
			cout << myName << " loses." << endl;
		}
	} else {
		cout << myName << " loses." << endl;
	}
	myGameCount++;
}

// Tally a loss resulting from the dealer's blackjack.
void Player::TallyLoss () {
	cout << myName << " loses to dealer blackjack." << endl;
	myGameCount++;
}

// Report the player's success ratio.
void Player::Report () {
	cout << myName << " won " << myWinCount 
		<< " out of " << myGameCount << endl;
}
