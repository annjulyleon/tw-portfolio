import unittest
from player import Player


class TestPlayer(unittest.TestCase):
    def test_create_player(self):
        new_player = Player('Human', 200)
        self.assertEqual(str(new_player), 'Player Human bank: 200')

    def test_add_card(self):
        new_player = Player('Human', 200)
        new_player.add_card('Two of Hearts')
        self.assertEqual(len(new_player.hand), 1)

    def test_remove_card(self):
        new_player = Player('Human', 200)
        new_player.add_card(['Two of Hearts'])
        new_player.add_card(['Three of Hearts'])
        new_player.remove_one()
        self.assertEqual(len(new_player.hand), 1)

    def test_add_to_bank(self):
        new_player = Player('Human', 200)
        new_player.add_to_bank(100)
        self.assertEqual(new_player.bank, 300)

    def remove_from_bank(self):
        new_player = Player('Human', 200)
        new_player.remove_from_bank(100)
        self.assertEqual(new_player.bank, 100)

    def remove_from_bank_more(self):
        new_player = Player('Human', 200)
        self.assertFalse(new_player.remove_from_bank(201))
        self.assertEqual(new_player.bank, 200)


if __name__ == "__main__":
    unittest.main()
