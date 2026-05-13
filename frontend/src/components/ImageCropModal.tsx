import React, { useState, useCallback } from 'react';
import Cropper, { Area } from 'react-easy-crop';
import './ImageCropModal.css';

interface ImageCropModalProps {
  imageSrc: string;
  onComplete: (croppedImage: Blob) => void;
  onCancel: () => void;
}

const ImageCropModal: React.FC<ImageCropModalProps> = ({ imageSrc, onComplete, onCancel }) => {
  const [crop, setCrop] = useState({ x: 0, y: 0 });
  const [zoom, setZoom] = useState(1);
  const [rotation, setRotation] = useState(0);
  const [croppedAreaPixels, setCroppedAreaPixels] = useState<Area | null>(null);

  const onCropComplete = useCallback((croppedArea: Area, croppedAreaPixels: Area) => {
    setCroppedAreaPixels(croppedAreaPixels);
  }, []);

  const createImage = (url: string): Promise<HTMLImageElement> =>
    new Promise((resolve, reject) => {
      const image = new Image();
      image.addEventListener('load', () => resolve(image));
      image.addEventListener('error', (error) => reject(error));
      image.setAttribute('crossOrigin', 'anonymous');
      image.src = url;
    });

  const getCroppedImg = async (
    imageSrc: string,
    pixelCrop: Area,
    rotation = 0
  ): Promise<Blob | null> => {
    const image = await createImage(imageSrc);
    const canvas = document.createElement('canvas');
    const ctx = canvas.getContext('2d');

    if (!ctx) {
      return null;
    }

    const rotRad = (rotation * Math.PI) / 180;
    const bBoxWidth =
      Math.abs(Math.cos(rotRad) * image.width) + Math.abs(Math.sin(rotRad) * image.height);
    const bBoxHeight =
      Math.abs(Math.sin(rotRad) * image.width) + Math.abs(Math.cos(rotRad) * image.height);

    canvas.width = bBoxWidth;
    canvas.height = bBoxHeight;

    ctx.translate(bBoxWidth / 2, bBoxHeight / 2);
    ctx.rotate(rotRad);
    ctx.translate(-image.width / 2, -image.height / 2);

    ctx.drawImage(image, 0, 0);

    const croppedCanvas = document.createElement('canvas');
    const croppedCtx = croppedCanvas.getContext('2d');

    if (!croppedCtx) {
      return null;
    }

    // Set the size to 512x512 for avatar
    const targetSize = 512;
    croppedCanvas.width = targetSize;
    croppedCanvas.height = targetSize;

    croppedCtx.drawImage(
      canvas,
      pixelCrop.x,
      pixelCrop.y,
      pixelCrop.width,
      pixelCrop.height,
      0,
      0,
      targetSize,
      targetSize
    );

    return new Promise((resolve) => {
      croppedCanvas.toBlob((blob) => {
        resolve(blob);
      }, 'image/jpeg', 0.95);
    });
  };

  const handleSave = async () => {
    if (!croppedAreaPixels) return;

    try {
      const croppedImage = await getCroppedImg(imageSrc, croppedAreaPixels, rotation);
      if (croppedImage) {
        onComplete(croppedImage);
      }
    } catch (e) {
      console.error('Error cropping image:', e);
    }
  };

  return (
    <div className="modal-overlay" onClick={onCancel}>
      <div className="modal crop-modal" onClick={(e) => e.stopPropagation()}>
        <div className="modal-header">
          <h2>Обрезать изображение</h2>
          <button className="modal-close" onClick={onCancel}>
            ✕
          </button>
        </div>
        <div className="modal-body">
          <div className="crop-container">
            <Cropper
              image={imageSrc}
              crop={crop}
              zoom={zoom}
              rotation={rotation}
              aspect={1}
              cropShape="round"
              showGrid={false}
              onCropChange={setCrop}
              onZoomChange={setZoom}
              onRotationChange={setRotation}
              onCropComplete={onCropComplete}
            />
          </div>
          <div className="crop-controls">
            <div className="control-group">
              <label>Масштаб</label>
              <input
                type="range"
                min={1}
                max={3}
                step={0.1}
                value={zoom}
                onChange={(e) => setZoom(Number(e.target.value))}
              />
            </div>
            <div className="control-group">
              <label>Поворот</label>
              <input
                type="range"
                min={0}
                max={360}
                step={1}
                value={rotation}
                onChange={(e) => setRotation(Number(e.target.value))}
              />
              <span>{rotation}°</span>
            </div>
          </div>
        </div>
        <div className="modal-footer">
          <button className="btn btn-secondary" onClick={onCancel}>
            Отмена
          </button>
          <button className="btn btn-primary" onClick={handleSave}>
            Применить
          </button>
        </div>
      </div>
    </div>
  );
};

export default ImageCropModal;
