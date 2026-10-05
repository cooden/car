from bs4 import BeautifulSoup
import os
from urllib.parse import urlparse, unquote
from selenium import webdriver
from selenium.webdriver.chrome.service import Service
from webdriver_manager.chrome import ChromeDriverManager
from selenium.webdriver.chrome.options import Options
import time
import requests

def download_image(url, folder_path):
    headers = {
        'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36'
    }
    try:
        response = requests.get(url, stream=True, headers=headers)
        response.raise_for_status()

        # 解析URL，获取不带查询参数的路径
        parsed_url = urlparse(url)
        path = parsed_url.path
        # 获取文件名，并进行URL解码，替换非法字符
        file_name = unquote(os.path.basename(path))
        # 移除文件名中可能存在的非法字符，例如问号、冒号等
        file_name = file_name.replace('?', '').replace(':', '').replace('/', '_').replace('\\', '_')

        # 确保文件名不为空
        if not file_name:
            print(f"Skipping download: Could not determine a valid filename for {url}")
            return

        full_file_path = os.path.join(folder_path, file_name)

        with open(full_file_path, 'wb') as file:
            for chunk in response.iter_content(chunk_size=8192):
                file.write(chunk)
        print(f"Downloaded: {full_file_path}")
    except requests.exceptions.RequestException as e:
        print(f"Error downloading {url}: {e}")
    except Exception as e:
        print(f"An unexpected error occurred while downloading {url}: {e}")

def crawl_website_images(url, output_folder):
    if not os.path.exists(output_folder):
        os.makedirs(output_folder)

    print(f"Starting to crawl: {url}")

    options = Options()
    options.add_argument('--headless')  # Run in headless mode
    options.add_argument('--disable-gpu')
    options.add_argument('--no-sandbox')
    options.add_argument('--disable-dev-shm-usage')

    # Set the path to your Chrome binary if it's not in the default location
    # For Windows, it might be 'C:\Program Files\Google\Chrome\Application\chrome.exe'
    # For macOS, it might be '/Applications/Google Chrome.app/Contents/MacOS/Google Chrome'
    # For Linux, it might be '/usr/bin/google-chrome'
    chrome_binary_path = ""
    if chrome_binary_path:
        options.binary_location = chrome_binary_path

    driver = None
    try:
        driver = webdriver.Chrome(service=Service(ChromeDriverManager().install()), options=options)
        driver.get(url)
        time.sleep(5)  # Give time for dynamic content to load

        soup = BeautifulSoup(driver.page_source, 'html.parser')

        # 查找所有图片标签
        img_tags = soup.find_all('img')
        print(f"Found {len(img_tags)} image tags.")
        for img in img_tags:
            img_url = img.get('src')
            if not img_url:
                img_url = img.get('data-src') # 尝试获取data-src属性

            if img_url and (img_url.startswith('http') or img_url.startswith('//')):
                # 如果是相对协议URL，添加https:
                if img_url.startswith('//'):
                    img_url = 'https:' + img_url
                print(f"Attempting to download: {img_url}")
                download_image(img_url, output_folder)
        print(f"Finished crawling: {url}")

    except Exception as e:
        print(f"Error accessing {url}: {e}")
    finally:
        if driver:
            driver.quit()

if __name__ == "__main__":
    zeekr_url = "https://www.zeekrlife.com/"
    output_directory_zeekr = "d:\\Study\\carc\\images\\zeekr"
    crawl_website_images(zeekr_url, output_directory_zeekr)