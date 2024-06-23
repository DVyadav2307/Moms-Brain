from flask import Flask, request, Response

app = Flask(__name__)

@app.route('/process', methods=['POST'])
def process():
    text_from_java = request.data.decode('utf-8')  # Get plain text data from request body
    
    # Example processing (you can replace this with your logic)
    reversed_text = text_from_java[::-1]

    # Sending back a plain text response
    return Response(reversed_text, content_type='text/plain')

if __name__ == '__main__':
    app.run(debug=True)
