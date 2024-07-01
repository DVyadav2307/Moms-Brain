""" TODO:
Dont Forget to 
install profanity check
initialize the server in a shell script
Use the same script file to run the bot also
end the server while terminating the program
"""
from flask import Flask, request, jsonify
from profanity_check import predict

app = Flask(__name__)

@app.route('/profane', methods=['GET'])
def reverse_string():

    chat_text = request.args.get('text')
    isProfane = predict([chat_text])

    if isProfane[0] == 0:
        return jsonify({'isProfane': "false"})
    else:
        return jsonify({'isProfane': "true"})

if __name__ == '__main__':
    app.run(debug=True)
