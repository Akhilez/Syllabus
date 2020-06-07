from bs4 import BeautifulSoup
import requests

must_download = True

url = 'https://jntuh.ac.in/syllabus'
results_length = 20  # Number of links per page
max_pages = 2
post_data = {'ajax': 1, 'searchin': 'title'}
key_words = ['M.Tech', 'Syllabus']

pdf_links = []

for i in range(0, max_pages * results_length, results_length):
    print(f'Page: {i / results_length}')

    post_data['offset'] = i
    html_data = BeautifulSoup(requests.post(url, post_data).text, "html.parser")

    hyperlinks = [link for link in html_data.find_all('a') if '.pdf' in link.get('href')]
    if len(hyperlinks) == 0:
        print(f'Last page: {i / results_length}')
        break

    for link in hyperlinks:
        href = link.get('href')
        if all([key_word in href for key_word in key_words]):
            pdf_links.append(href)
            print(href)


def download_pdf(link):
    file_name = 'data/' + link.split("academics/", 1)[1]
    with open(file_name, "wb") as file:
        response = requests.get(link)
        file.write(response.content)


if must_download:
    import threading
    for link in pdf_links:
        print(f"Downloading PDF from: {link}")
        threading.Thread(target=download_pdf, args=(link,)).start()
