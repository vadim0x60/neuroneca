# Neuroneca

Promoting [Stoicism](https://en.wikipedia.org/wiki/Stoicism) on Twitter with Clojure. Read my tweets [here](https://twitter.com/Neuroneca).

## Make your own bot

1. Clone this repository.
2. Install [leiningen](https://leiningen.org/) if you haven't already.
3. Add or remove books in `resources/texts` if you want. With a right collection of source texts you can make a bot promoting any cause you care about. Use this power responsibly.
4. Clean up your source texts. The program will try to ignore obviously irrelevant text like chapter numbers, but if your books have entirely irrelevant sections like copyright notices it's probably a good idea to remove them or you will end up with an IP lawyer bot.
5. `lein run --learn` to train the bot's intelligence based on your books.
6. Connect the bot to your account by defining `APP_CONSUMER_KEY`, `APP_CONSUMER_SECRET`, `USER_ACESS_TOKEN` and `USER_ACESS_TOKEN_SECRET` environment variables
7. `lein run` to tweet.

## License

Copyright © 2017 Vadim Liventsev

Distributed under the MIT License, see `LICENSE`.
