#include <iostream>
#include <vector>

using namespace std;

class Board 
{
	public:
        static const int DEFAULT_SIZE = 19;
        Board(); // default size 19x19
		Board(int s);
		void SetCell(int player, int row, int col, char c);
		void Print(int player);
		
	private:
		int size;
		vector<vector<char> > vrow; // vector of vectors of type <char>
};

Board::Board()
    : size(DEFAULT_SIZE),
      vrow(DEFAULT_SIZE, vector<char> (DEFAULT_SIZE, '.'))
{}

Board::Board(int s)
	: size(s), 
	  vrow(s, vector<char> (s, '.'))
{}

void Board::SetCell(int player, int row, int col, char c) {
	if (player == 0) {
		vrow[row][col] = c; // count from top left
	} else if (player == 1) {
        int oppRow = vrow.size() - row - 1;
        int oppCol = vrow.size() - col - 1;
		vrow[oppRow][oppCol] = c; // count from bottom right
	} else {
		cerr << "Invalid player number." << endl;
	}
}

void Board::Print(int player) {
	if (player == 0) {
		for (int i = 0; i < vrow.size(); i++) { // rows
			for (int j = 0; j < vrow[i].size(); j++) { // columns
				cout << vrow[i][j];
			}
			cout << endl;
		}
	} else {
		for (int i = vrow.size()-1; i >= 0; i--) {     // start from bottom row
			for (int j = vrow[i].size()-1; j >= 0; j--) { // start from right col
				cout << vrow[i][j];
			}
			cout << endl;
		}
	}
}

int main() {
	Board board(3); // create 3x3 board
    cout << "New board view to player 0:" << endl;
    board.Print(0);
    cout << "New board view to player 1:" << endl;
    board.Print(1);
    cout << endl;

    cout << "Setting cell [0,2] to '0' for player 0." << endl;
	board.SetCell(0, 0, 2, '0');
    cout << "New board view to player 0:" << endl;
	board.Print(0);
    cout << "New board view to player 1:" << endl;
    board.Print(1); // flip
	cout << endl;

    cout << "Setting cell [0,1] to '1' for player 1." << endl;
	board.SetCell(1, 0, 1, '1');
    cout << "New board view to player 0:" << endl;
	board.Print(0);
    cout << "New board view to player 1:" << endl;
	board.Print(1); // flip
    cout << endl;

    Board board2;
    cout << "New 19x19 board looks like:" << endl;
    board2.Print(0);
    cout << endl;
    board2.SetCell(0,18,0,'a');
    board2.SetCell(1,18,0,'b');
    board2.SetCell(0,18,18,'c');
    board2.SetCell(0,0,0,'d');
    board2.SetCell(1,18,18,'e');
    board2.SetCell(1,0,0,'f');
    board2.SetCell(0,6,6,'g');
    board2.SetCell(1,12,6,'h');
    board2.SetCell(1,6,12,'i');
    board2.SetCell(0,12,12,'j');
    cout << "For player 0: rows 0,6,12,18 should be:" << endl
         << "e.................b" << endl
         << "......g.....h......" << endl
         << "......i.....j......" << endl
         << "a.................f" << endl << endl;
    board2.Print(0);
    cout << endl;
    cout << "For player 1: rows 0,6,12,18 should be:" << endl
         << "f.................a" << endl
         << "......j.....i......" << endl
         << "......h.....g......" << endl
         << "b.................e" << endl << endl;
    board2.Print(1); // flip
    cout << endl;
	return 0;
}
