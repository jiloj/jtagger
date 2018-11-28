## jtagger

### Categories

The defined semantic categories for this service are *history*, *sports*, *geography*, *culture*, *science*, *politics*, *religion*, *words*, *music*, *art*, *food*, *opera*, *literature*, *tv/film*, *theatre*. These categories are intended to span the entirety of all jeopardy clues and all clues will fall within one of these categories. Categories will be defined in more detail below with common conventions on them. For example, *culture* implies anything that is generally known through keen observation and would not be known through dedicated study. This is something like business information or americana.

#### History

* Most historical events
* Past political events, especially when said person is dead
* Wars and battles


#### Sports

* Related to organized sports teams or leagues
* Rules and regulations of the game
* Events and type of sport
* Board games, card games, and other non physical games are still considered sport for this purpose
* Location of the olympics goes here as well, unless there is identifiable information that can make the semantic vector different


#### Geography

* This is human geography and physical geography such as rivers and mountains
* Physical processes will tend to fall under *Science* however.
* College questions related to location tend to be here as well
* Socio-linguistics falls here than under *Words*
* Currency questions
* Landmarks


#### Culture

* Business and brand information
* NGO and non-profit information
* Reality tv and pop culture
* Cultural icons, including comic book tidbits
* Holidays
* Quotes will tend to be here too, but are not deterministically so
* American nursery rhymes


#### Science

* Technical questions about science or math topics in a broad sense
* Questions about scientists themselves
* Questions about botany or zoology
* Questions about specific technical museums or their names / information
* Inventions or patents can go here as well


#### Politics

* Related to current day politics
* Political party information here and abroad
* Court cases, except for foundational or historic ones from more than a century back
* Modern day government affairs such as the army or logistics


#### Religion

* Any having to do with belief systems, holy books, and so forth
* This does not include mythology as noted in the *Classics* section


#### Words

* Linguistic questions, such as etymology
* Word play
* Common sayings and idioms
* Terminology of some sort
* Vocabulary based questions
* These tend to be things not requiring technical knowledge, and are known in common parlance


#### Music

* Contemporary music (not opera or theatrical performances)
* Songs, albums, artists, labels, and related


#### Art

* NOTE: This is a fairly specialized category that is not coming up very often. Consider merging with other categories.
* Art works and artists
* Art supplies and technical terms


#### Food

* Food and drink related clues
* Plant related clues can come up here, if it pertains to the eating of the plant
* Kitchen and cooking supplies


#### Opera

* NOTE: This is a fairly specialized category that is not coming up very often. Consider merging with other categories.


#### Literature

* Includes comic books
* Includes shakespeare
* News paper activities such as columnists, and editors
* Philosophical works will go in here, such as my boi Sartre


#### TV/Film

* Related to tv shows, awards, and movies
* People are also included in this, and more so if they are primarily known for their tv / movie career


#### Theatre

* NOTE: This is a fairly specialized category that is not coming up very often. Consider merging with other categories.
* Plays, dramas, musicals, ballet, etc. unless literature is a better fit such as for Shakespeare
* Basically theatrical performance other than tv or film.
* Comedians and comedies will usually fall under here too depending on their other roles


#### Classics

* Ancient Greek texts, persons, and events
* Ancient Roman texts, persons, and events
* Mythology in general. Religion should be reserved for current time religions and not past civilizations. This includes non roman or greek mythology.
* Ancient history structures, such as the parthenon


### Other Notes

These are other notes and thoughts that I am coming across as I am starting to undertake the task of annotation. The resolution to these thoughts will be listed below them.

* There are several clues that are an extreme split between categories, in that the same word is used in various contexts, and the Jeopardy! writers take advantage of this. The current approach is to arbitrarily pick one of the possibilities. This should codified for future annotation.
* Some categories are improperly defined between themselves such as theatre and literature, opera and music, politics and history, and classics and religion. Furthermore, a category like words intersects with all of them. These distinctions should be made more clear.
