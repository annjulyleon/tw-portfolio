from card import *
from deck import *
from player import *

if __name__ == '__main__':
    player = Player('Human', 900)
    dealer = Player('Dealer', 0)

    round_on = True
    # Game consist of rounds. Game can end after any round.
    while round_on:
        # At the begining of the each round deck is recreated and shuffled and hands are cleared
        deck = Deck()
        deck.shuffle()
        player.hand = []
        dealer.hand = []
        player_won = False
        dealer_won = False
        print(player)

        # we must check if player has a money to play
        if player.bank == 0:
            print(f'Sorry, Player, you does not have any money to play!')
            break

        # Game ask player to place his bet. Bet is just a number player enter via input field.
        while True:
            bet = int(input(f'Player {player.name}, place your bet! '))
            if bet > player.bank:
                print(f'Sorry, you must enter ammount less than your bank ammount')
            else:
                break
        print(f'Player {player.name} placed {bet}')

        # Game deal player and dealer two cards.
        for i in range(2):
            player.hand.append(deck.deal_one())
            dealer.hand.append(deck.deal_one())

        # Game shows Player their cards and one card that dealer has.
        print(
            f'Player {player.name}, you have cards {player.display_hand()} with total value of [{player.hand_value()}]')
        print(
            f"Dealer have one card facing up: [{str(dealer.hand[0])}] with total value of [{11 if dealer.hand[0].rank =='Ace' else str(dealer.hand[0].value)}].")

        # After cards are dealt game must check if player has a natural blackjack.
        # If player has blackjack and dealer has not card with value 10 or Ace, than player wins the round and takes double bet.
        if player.hand_value() == 21 and (dealer.hand[0].value != 10 or dealer.hand[0].value != 1):
            print('Congratulations! You have natural blackjack and dealer cannot possibly beat it! You take double bet as a reward')
            player.add_to_bank(bet * 2)
            player_won = True
        # If player has blackjack and dealer condition is false than game ask if player want to take 1,5 of reward or continue to play .
        elif player.hand_value() == 21 and (dealer.hand[0].value == 10 or dealer.hand[0].value == 1):
            choice = bool(input('You have natural blackjack, but dealer has a chance on blackjack too! You can take 1,5 of your bet as reward right now or continue to play? Continue to play? Enter True or False '))
            if choice:
                continue
            else:
                print('You have chosen take 1,5 of your bet!')
                player.add_to_bank(bet * 1.5)
                player_won = True
        # In any other cases loop for hit and stand begins.
        else:
            while True:
                # It's a human player turn. He can hit or stand.
                hit = input("Do you want to hit? Enter Y or N: ")
                print(hit)
                # if he chose hit, card is added.

                if hit == 'Y':
                    player.add_card(deck.deal_one())
                    print(
                        f'Player {player.name}, you have cards {player.display_hand()} with total value of [{player.hand_value()}]')
                    # If result is more that 21, than player has lost, remove bet from bank, break from loop.
                    if player.hand_value() > 21:
                        print(
                            f"Player {player.name} is a BUST! Dealer has won")
                        dealer_won = True
                        player.remove_from_bank(bet)
                        break
                    # If result is 21 than player has blackjack and take double bet, and break
                    elif player.hand_value() == 21:
                        print(
                            f'Player {player.name} has blackjack! Player has won!')
                        player_won = True
                        player.add_to_bank(bet*2)
                        break
                    # if nothing above is true, than turn is passed to dealer
                    else:
                        continue
                else:
                    # if player refused to hit, than turn is passed to dealer.
                    # dealer must take cards until his hand_value is at least 17.
                    print(
                        f'Dealer has {dealer.display_hand()} with total value of [{dealer.hand_value()}]')
                    print(dealer.hand)
                    while (not player_won or not dealer_won) and dealer.hand_value() < 17:
                        dealer.add_card(deck.deal_one())
                    # if after adding cards dealer hand_value more than 21, than dealer has lost and player takes double bet
                    print(
                        f'Dealer has cards with total value of {dealer.hand_value()}')
                    if dealer.hand_value() > 21:
                        print(
                            f"Dealer is a BUST! Player has won")
                        player_won = True
                        player.add_to_bank(bet*2)
                        break
                    # if dealer has blackjack, he wins and player remove bet from account
                    elif dealer.hand_value() == 21:
                        print(
                            f'Dealer has blackjack! Dealer has won!')
                        dealer_won = True
                        player.remove_from_bank(bet)
                        break
                    # if dealer hand is less than 21 (or else), than we must know, who is closer to 21, or if there is a tie.
                    elif dealer.hand_value() < 21:
                        if dealer.hand_value() == player.hand_value():
                            print(
                                f'There is a tie! No one wins, player take bet to account')
                            player.add_to_bank(bet)
                            break
                        else:
                            if (21 - dealer.hand_value()) > (21 - player.hand_value()):
                                print(
                                    f'Player Human is closer to 21 than dealer. Player Human has won!')
                                player_won = True
                                player.add_to_bank(bet*2)
                                break
                            else:
                                print(
                                    f'Player Dealer is closer to 21 than Human. Player Dealer has won!')
                                dealer_won = True
                                player.remove_from_bank(bet)
                                break

        cont = input('Wanna another round? Y or N ')
        if cont == 'N':
            round_on = False
            break
        else:
            continue
