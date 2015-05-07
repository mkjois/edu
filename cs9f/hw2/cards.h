#ifndef _CARDS
#define _CARDS
#include <iostream>
#include <vector>
#include <string>

using namespace std;

class Card {
public:
	// Construct a card initialized to the ace of clubs.
	Card ();

	// Change the card to the nth card in the sequence
	// ace of clubs, 2 of diamonds, 3 of hearts, 4 of spades,
	// 5 of clubs, ..., queen of spades,
	// king of clubs, ace of diamonds, 2 of hearts, 3 of spades,
	// 4 of clubs, ..., jack of spades,
	// queen of clubs, king of diamonds, ace of hearts, 2 of 
	// spades, etc.
	void ChangeCard (int n);

	// Return the card's value in blackjack: 11 if it's an ace,
	// 10 if it's a face card (king, queen, or jack), and the 
	// card's face value (2, 3, ..., or 10) otherwise.
	int Value ();

	// Return the name of the card.
	string Name ();

	// Return true if the card is an ace, and return false 
	// otherwise.
	bool IsAce ();

	// Return true if the card is a face card (king, queen, or 
	// jack), and return false otherwise.
	bool IsFaceCard ();

	// Return true if the card outranks the argument card.
	// One card outranks another if it has a higher value.
	// Any face card outranks a 10; a king outranks a queen or 
	// jack; and a queen outranks a jack.
	bool Outranks (Card c);

	// Return true if the card and the argument card are equal 
	// in value.
	bool EqualsInRank (Card c);

	// Return true if the card and the argument card are the 
	// same suit.
	bool IsSameSuit (Card c);

private:
	int myRank;
	int mySuit;
};

class Deck {
public:
	// Construct a deck of 52 cards.
	// If a true argument is given, it causes debugging output
	// to be printed to cerr every time a card is dealt or the deck
	// shuffled.
	Deck (bool debug=false);

	// Shuffle the deck, i.e. randomly permute its cards.
	void Shuffle ();

	// Print the contents of the deck.
	void Print (ostream &output);

	// Return the top card of the deck, and remove that card from
	// the deck.  If the deck is empty, first shuffle the already-
	// dealt cards and deal from them.
	Card Deal ();

	// Return true if the deck is empty and false otherwise.
	bool IsEmpty ();

private:
	vector <Card> myCards;
	int myTop;
	bool debugging;
};
#endif
