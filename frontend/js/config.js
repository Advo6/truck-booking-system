const API_BASE_URL = 'http://localhost:8080/api';
window.API_BASE_URL = API_BASE_URL;

const CONFIG = {
  businessName: 'Noko Transport',
  businessPhone: '+27821234567',
  businessEmail: 'info@nokotransport.co.za',
  businessAddress: '123 Transport Road, Johannesburg, South Africa',
  whatsappUrl: 'https://wa.me/27821234567',
  mapsEmbed: 'https://www.google.com/maps/embed?pb=!1m18!1m12!1m3!1d3580.0!2d28.0473!3d-26.2041!2m3!1f0!2f0!3f0!3m2!1i1024!2i768!4f13.1!3m3!1m2!1s0x0%3A0x0!2zMjbCsDEyJzE0LjgiUyAyOMKwMDInNTAuMyJF!5e0!3m2!1sen!2sza!4v1234567890',
  truckImages: ['images/truck1.jpg', 'images/truck2.jpg']
};

/** Default fleet when the backend API is unavailable. */
const FALLBACK_TRUCKS = [
  {
    id: 1,
    truckNumber: 'Truck 1',
    registration: 'CA 123-456 GP',
    capacity: '34 Tonnes',
    description: 'Mercedes-Benz Actros Code 14 — ideal for long-haul freight and heavy cargo transport.',
    status: 'AVAILABLE',
    image: 'images/truck1.jpg'
  },
  {
    id: 2,
    truckNumber: 'Truck 2',
    registration: 'CA 789-012 GP',
    capacity: '34 Tonnes',
    description: 'Volvo FH16 Code 14 — reliable heavy-duty truck for construction materials and bulk deliveries.',
    status: 'AVAILABLE',
    image: 'images/truck2.jpg'
  }
];
