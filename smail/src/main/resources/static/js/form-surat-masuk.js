function chooseFile() {
  document.getElementById("file-upload-input").click();
}

// Fungsi untuk menampilkan ikon file yang diunggah
function handleFileInput(event) {
    const fileInput = event.target;
    const file = fileInput.files[0];
    
    // Periksa apakah ada file yang dipilih
    if (file) {
        const fileExtension = getFileExtension(file.name);
        const imageSrc = getImageSource(fileExtension);
        const uploadIllustration = document.querySelector('.uploaded-file-container');
        
        // Perbarui sumber gambar
        uploadIllustration.innerHTML = `<img class="uploaded-file-icon" src="${imageSrc}" alt="${file.name}" />`;
    }
}

// Fungsi untuk mendapatkan ekstensi file
function getFileExtension(filename) {
    return filename.split('.').pop().toLowerCase();
}

// Fungsi untuk mendapatkan sumber gambar berdasarkan ekstensi file
function getImageSource(extension) {
    switch (extension) {
        case 'png':
        case 'jpg':
        case 'jpeg':
        case 'gif':
            return '/images/image-logo.png'; // Ganti dengan sumber ikon gambar Anda
        case 'pdf':
            return '/images/image-logo.png'; // Ganti dengan sumber ikon PDF Anda
        // Tambahkan kasus lain untuk jenis file lain jika diperlukan
        default:
            return '/images/image-logo.png'; // Ikon file default
    }
}
