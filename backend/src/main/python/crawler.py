import requests
from bs4 import BeautifulSoup
import os
import requests
from urllib.parse import urlparse, unquote

def download_image(url, folder_path):
    try:
        response = requests.get(url, stream=True)
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

    try:
        response = requests.get(url)
        response.raise_for_status()
        soup = BeautifulSoup(response.text, 'html.parser')

        # 查找所有图片标签
        img_tags = soup.find_all('img')
        for img in img_tags:
            img_url = img.get('src')
            if img_url and img_url.startswith('http'):
                download_image(img_url, output_folder)

    except requests.exceptions.RequestException as e:
        print(f"Error accessing {url}: {e}")

if __name__ == "__main__":
    lynkco_url = "https://www.lynkco.com.cn/"
    output_directory_lynkco = "d:\\Study\\carc\\images\\lync_co"
    crawl_website_images(lynkco_url, output_directory_lynkco)



