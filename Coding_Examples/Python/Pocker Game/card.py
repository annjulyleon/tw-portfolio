'''
A Card class define card suit, rank and value.
Input 
string suit
string rank
'''
card_values = {'Two': 2, 'Three': 3, 'Four': 4, 'Five': 5, 'Six': 6, 'Seven': 7, 'Eight': 8, 'Nine': 9,
               'Ten': 10, 'Jack': 10, 'Queen': 10, 'King': 10, 'Ace': 1}


class Card:

    def __init__(self, suit, rank):
        self.suit = suit
        self.rank = rank
        self.value = card_values[rank]

    def __str__(self):
        return self.rank + " of " + self.suit
