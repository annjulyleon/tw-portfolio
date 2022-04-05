from card import *


class Player:

    def __init__(self, name, bank):
        self.name = name
        self.bank = bank
        self.hand = []

    def __str__(self):
        return f'Player {self.name} bank: {self.bank}'

    def add_card(self, card):
        self.hand.append(card)

    def remove_one(self):
        return self.hand.pop()

    def display_hand(self):
        return [str(x) for x in self.hand]

    def hand_value(self):
        if any(x.rank == 'Ace' for x in self.hand) and sum([x.value for x in self.hand]) <= 11:
            #print('Ace is in hand and total is less than 11, so ace is now 11')
            return sum([x.value for x in self.hand]) + 10
        else:
            return sum([x.value for x in self.hand])

    def add_to_bank(self, ammount):
        self.bank = self.bank + ammount
        print(f'Player {self.name} new bank balance: {self.bank}')

    def remove_from_bank(self, ammount):
        if ammount > self.bank:
            return False
        else:
            self.bank = self.bank - ammount
            return True


if __name__ == "__main__":
    new_player = Player('Human', 200)
    card1 = Card('Clubs', "Seven")
    card2 = Card('Clubs', "Six")
    card3 = Card('Clubs', "Two")
    new_player.hand.append(card1)
    new_player.hand.append(card2)
    # print(
    #    f'Player {new_player.name}, you have cards {[str(x) for x in new_player.hand]} with total value of {sum([x.value for x in new_player.hand])}')
    print(new_player.display_hand())
    print(new_player.hand_value())
    print(new_player.hand[0])
    print(new_player.hand[0].value)
    print(any(x.rank == 'Ace' for x in new_player.hand))
    if new_player.hand_value() < 17:
        new_player.add_card(card3)
        print(new_player.display_hand())
