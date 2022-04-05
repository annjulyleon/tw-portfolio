import unittest
from deck import Deck


class TestDeck(unittest.TestCase):
    def test_create_deck(self):
        new_deck = Deck()
        self.assertEqual(len(new_deck), 52)

    def test_first_card(self):
        new_deck = Deck()
        first_card = new_deck.all_cards[0]
        self.assertEqual(str(first_card), 'Two of Hearts')

    def test_last_card(self):
        new_deck = Deck()
        first_card = new_deck.all_cards[-1]
        self.assertEqual(str(first_card), 'Ace of Clubs')

    def test_shuffle_deck(self):
        new_deck = Deck()
        before_shuffle = str(new_deck.all_cards[0])
        new_deck.shuffle()
        self.assertNotEqual(before_shuffle, str(new_deck.all_cards[0]))


if __name__ == "__main__":
    unittest.main()
