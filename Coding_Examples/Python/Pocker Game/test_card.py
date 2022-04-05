import unittest
from card import Card, card_values


class TestCard(unittest.TestCase):
    def test_create_card(self):
        suit = "Hearts"
        rank = "Two"
        a = Card(suit, rank)
        self.assertEqual(str(a), 'Two of Hearts')

    def test_card_rank(self):
        suit = "Clubs"
        rank = "Seven"
        a = Card(suit, rank)
        self.assertEqual(a.value, 7)


if __name__ == "__main__":
    unittest.main()
