#include <iostream>
using namespace std;
#include "cards.h"
#include "21game.h"
int main () {
	Deck d(false);
	Group21 group;
	int numGames;
	cout << "How many games? ";
	cin >> numGames;
	for (int k=1; k<=numGames; k++) {
		group.PlayGame (d);
	}
	group.Report ();
	return 0;
}
