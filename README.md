# The Snarfer

A snarfer is a program that pulls data from some other source, generally not in the way that it was intended to be accessed.  In this case, the snarfer pulls articles and images from news sites such as CNN and BBC.  News sources generally do not provide any programmatic way to get this data, so we parse the HTML to get the correct image and the associated text for any given article.

The first revision of the program (2005) followed the link tree to find articles.  This was error prone and eventually produced fewer articles as sites started to incorporate Javascript links.  In 2010 the indexing code was changed to use RSS feeds.  In 2005 these had not produced sufficient data, but by 2010 they listed more articles than we were getting from walking the HTML pages.

The current version consists of these steps:
- A list of articles is created from the provided RSS feeds for each site.
- Each HTML page is loaded and the article text is scraped off.  The article image is identified by its proportions and size.
- The data is saved into the DB from the internal structures in a single transaction.
- The data is then exported from the DB to files on disk as needed.

The snarfer runs daily and if it runs twice it overwrites the data for that day.  Initialization parameters are stored in the `snarfer.ini` file which follows a standard windows ini format.

The idea behind the snarfer is simple - create a large archive of images and articles that are time specific to be used in artworks that change each day to reflect current events.  Most news organizations do not provide a consistent view of what their sites looked like in the past.  The snarfer keeps a record so that we can recreate any day that the snarfer ran and show how the artworks would have looked on that day.
