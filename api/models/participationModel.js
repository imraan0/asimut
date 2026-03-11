const { DataTypes } = require('sequelize')
const { sequelize } = require('../config/database')

const Participation = sequelize.define('participation', {
    id: {
        type: DataTypes.INTEGER,
        primaryKey: true,
        autoIncrement: true
    },
    est_responsable: {
        type: DataTypes.BOOLEAN,
        defaultValue: false
    },
    date_debut: {
        type: DataTypes.DATE,
        allowNull: false
    },
    date_fin: {
        type: DataTypes.DATE,
        allowNull: true     //on peut ne pas connaître la date de fin de la participation
    }
}, {
    timestamps: false,
    underscored: true,
    indexes: [
        {
            unique: true,
            fields: ['eleve_id', 'projet_id']
        }
    ]
})

module.exports = Participation