# ReVerbHttp
ReVerb is a relation extractor software (https://github.com/knowitall/reverb).

It works well but takes some time to start (about 10 seconds), so this is a simple Http server to query ReVerb quickly, which is important when you want to give ReVerb only a small amount of text (one question for example).


# Building
Open the project in eclipse then add reverb library as an external jar. Then you can compile and start the program

# Example
First start the program, then you can query ReVerb with : curl http://localhost:8002/ --data-urlencode "text=Who is the daughter of Bill Clinton married to?"